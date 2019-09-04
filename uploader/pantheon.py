#!/usr/bin/python3
import os
import requests
import argparse
import getpass
import logging
import yaml
import socket
import base64
import sys
import requests
import pathlib
import fnmatch
import glob
from pathlib import PurePath

DEFAULT_SERVER = 'http://localhost:8080'
if "PANTHEON_SERVER" in os.environ:
    DEFAULT_REPOSITORY = 'gitImport'
else:
    DEFAULT_REPOSITORY = getpass.getuser()
DEFAULT_USER = 'demo'
DEFAULT_PASSWORD = base64.b64decode(b'ZGVtbw==').decode()
DEFAULT_LINKS = False
CONFIG_FILE = 'pantheon2.yml'

HEADERS = {'cache-control': 'no-cache',
           'Accept': 'application/json'}


def _generate_data(jcr_primary_type, base_name, path_name, asccidoc_type):
    """
    Generate the data object for the API call.
    """
    data = {}
    if jcr_primary_type:
        data["jcr:primaryType"] = jcr_primary_type
    if base_name:
        data["jcr:title"] = base_name
        data["jcr:description"] = base_name
    if path_name:
        data["pant:originalName"] = path_name
    if asccidoc_type:
        data["asciidoc@TypeHint"] = asccidoc_type

    return data


def _info(message):
    """
    Print an info message on the console. Warning messages are cyan
    """
    print("\033[96m{}\033[00m" .format(message))


def _warn(message):
    """
    Print a warning message on the console. Warning messages are yellow
    """
    print("\033[93m{}\033[00m" .format(message))


def _error(message):
    """
    Print an error message on the console. Warning messages are red
    """
    print("\033[91m{}\033[00m" .format(message))


def _print_response(response_code, reason):
    """
    Prints an http response in the appropriate terminal color
    """
    if 200 <= response_code < 300:
        _info(str(response_code) + " " + reason)
    elif response_code >= 500:
        _error(str(response_code) + " " + reason)
    else:
        print(response_code, reason)


parser = argparse.ArgumentParser(formatter_class=argparse.RawTextHelpFormatter, description='''\
Red Hat bulk upload module for Pantheon 2. This tool will scan a directory recursively and upload relevant files.

Both this uploader and Pantheon 2 are ALPHA software and features may update or change over time.

''')
parser.add_argument('push', nargs='+', help='Type of operation, default push')
parser.add_argument('--server', '-s', help='The Pantheon server to upload modules to, default ' + DEFAULT_SERVER)
parser.add_argument('--repository', '-r', help='The name of the Pantheon repository, default is username_hostname (' + DEFAULT_REPOSITORY + ')')
parser.add_argument('--user', '-u', help='Username for authentication, default \'' + DEFAULT_USER + '\'', default=DEFAULT_USER)
parser.add_argument('--password', '-p', help='Password for authentication, default \'' + DEFAULT_PASSWORD + '\'. If \'-\' is supplied, the script will prompt for the password.', default=DEFAULT_PASSWORD)
parser.add_argument('--directory', '-d', help='Directory to upload, default is current working directory. (' + os.getcwd() + ')', default=os.getcwd())
parser.add_argument('--links', '-l', help='Resolve symlinks when searching for files to upload', action='store_const', const=True)
parser.add_argument('--verbose', '-v', help='Print information that may be helpful for debugging', action='store_const', const=True)
parser.add_argument('--dry', '-D', help='Dry run; print information about what would be uploaded, but don\'t actually upload', action='store_const', const=True)
parser.add_argument('--sandbox', '-b', help='Push to the user\'s personal sandbox. This parameter overrides --repository', action='store_const', const=True)
parser.add_argument('--sample', '-S', help='Print a sample pantheon2.yml file to stdout (which you may want to redirect to a file).', action='version', version='''\
# Config file for Pantheon v2 uploader
## server: Pantheon server URL
## repository: a unique name, which is visible in the user facing URL
## followlinks: true/false
## If you set the followLinks to true then all the asciidoc files resolved via symlinks will be included and uploaded.
## If you set the followLinks to false then all the asciidoc files resolved via symlinks will be ignored and not be uploaded.

server: http://localhost:8080
repository: pantheonSampleRepo
followlinks: true

titles:
 - master.adoc

modules:
 - shared/legal.adoc
 - shared/foreword.adoc
 - modules/*.adoc

resources:
 - shared/*.jpg
 - shared/*.svg
''')
args = parser.parse_args()

logStr = 'DEBUG' if args.verbose is not None else 'WARNING'
numeric_level = getattr(logging, logStr, None)
if not isinstance(numeric_level, int):
    raise ValueError('Invalid log level: %s' % args.log)
logger = logging.getLogger(__name__)
logger.setLevel(numeric_level)
logger.addHandler(logging.StreamHandler())

pw = args.password
if pw == '-':
    pw = getpass.getpass()

config = None

if not os.path.exists(args.directory):
    raise ValueError("Directory not found {}".format(args.directory))

try:
    config = yaml.safe_load(open(args.directory + '/' + CONFIG_FILE))
except FileNotFoundError:
    logger.warning('Could not find a valid config file(' + CONFIG_FILE + ') in this directory; all files will be treated as resource uploads.')
logger.debug('config: %s', config)


def resolveOption(parserVal, configKey, default):
    if parserVal is not None:
        return parserVal
    elif config is not None and configKey in config:
        return config[configKey]
    else:
        return default


def exists(path):
    """Makes a head request to the given path and returns a status_code"""
    try:
        resp = requests.head(path)
        logger.debug('HEAD request to remote server. Response status_code: %s', resp.status_code)
        return resp.status_code < 400
    except Exception:
        return False


def remove_trailing_slash(path):
    """Removes the trailing slash from path if exists and returns a string"""
    if path.endswith('/'):
        path = path[:-1]
    return path


def find_files(patterns, directory):
    """
    Finds files matching patterns defined in patheon2.yml. To match everything
    under a subdirectory, use pattern:
    subdir/**/*

    Paramters:
    patterns (list): A list of file path patterns
    directory (string): A directory that contains files to be uploaded

    Returns:
    list: A list of files matched
    """
    files = []

    if patterns:
        for pattern in patterns:
            for file in glob.iglob(directory + '/' + pattern, recursive=True):
                #logger.debug('file %s', file)
                file = PurePath(file)
                name = file.name
                if name == 'pantheon2.yml':
                    continue
                if os.path.isfile(file):
                    files.append(file)

    return files


def process_file(path, filetype):
    """
    Processes the matched files and upload to pantheon through sling api call

    Paramters:
    path (string): A file patch
    filetype (string): A type of file(titles, modules or resources)

    Returns:
    list: It returns a list with value of the API call status_code and reason
    """
    global processed_files
    isTitle = True if filetype == 'titles' else False
    isModule = True if filetype == 'modules' else False
    isResource = True if filetype =='resources' else False
    content_root = 'sandbox' if args.sandbox else 'repositories'
    url = server + "/content/" + content_root + "/" + repository

    if isModule or isTitle or isResource:
        path = PurePath(path)
        base_name = path.stem

        ppath = path
        hiddenFolder = False
        while not ppath == PurePath(args.directory):
            logger.debug('ppath: %s', str(ppath.stem))
            if ppath.stem[0] == '.':
                hiddenFolder = True
                break
            ppath = ppath.parent
        if hiddenFolder:
            logger.debug('Skipping %s because it is hidden.', str(path))
            logger.debug('')
            #continue

        # parent directory
        parent_dir_str = str(path.parent.relative_to(args.directory))
        if parent_dir_str == '.':
            parent_dir_str = ''
        logger.debug('parent_dir_str: %s', parent_dir_str)
        # file becomes a/file/name (no extension)

        if parent_dir_str:
            url += '/' + parent_dir_str

        logger.debug('base name: %s', base_name)

        # Asciidoc content (treat as a module)
        if isModule:
            print(path)
            url += '/' + path.name
            logger.debug('url: %s', url)
            jcr_primary_type = "pant:module" if isModule else "pant:title"
            data = _generate_data(jcr_primary_type, base_name, path.name, asccidoc_type="nt:file")
            # This is needed to add a new module revision, otherwise it won't be handled
            data[":operation"] = "pant:newModuleRevision"
            files = {'asciidoc': ('asciidoc', open(path, 'rb'), 'text/x-asciidoc')}

            # Minor question: which is correct, text/asciidoc or text/x-asciidoc?
            # It is text/x-asciidoc. Here's why:
            # https://tools.ietf.org/html/rfc2045#section-6.3
            # Paraphrased: "If it's not an IANA standard, use the 'x-' prefix.
            # Here's the list of standards; text/asciidoc isn't in it.
            # https://www.iana.org/assignments/media-types/media-types.xhtml#text

            if not args.dry:
                r = requests.post(url, headers=HEADERS, data=data, files=files, auth=(args.user, pw))
                _print_response(r.status_code, r.reason)
            processed_files.append(path)
            logger.debug('')
        elif isResource:
            # determine the file content type, for some common ones
            file_type = None
            if path.suffix in ['.adoc', '.asciidoc']:
                file_type = "text/x-asciidoc"
            # Upload as a regular file(nt:file)
            print(path)
            logger.debug('url: %s', url)
            jcr_primary_type = "nt:file"
            data = _generate_data(jcr_primary_type, base_name, path.name, asccidoc_type=None)
            files = {path.name: (path.name, open(path, 'rb'), file_type)}
            if not args.dry:
                r = requests.post(url, headers=HEADERS, files=files, auth=(args.user, pw))
                _print_response(r.status_code, r.reason)
            processed_files.append(path)
            logger.debug('')

    return r.status_code, r.reason


def get_unspecified_files(directory, processed_files, follow_links=True):
    """Collects files from the given directory that were not specified in patheon2.yml file and returns a list"""
    unspecified_files = []
    for root, dirs, files in os.walk(directory, follow_links):
        for file in files:
            if file == 'pantheon2.yml':
                continue

            path = PurePath(root + '/' + file)
            if path not in processed_files:
                unspecified_files.append(path)

    return unspecified_files


if "PANTHEON_SERVER" in os.environ:
    server = os.environ["PANTHEON_SERVER"]
else:
    server = resolveOption(args.server, 'server', DEFAULT_SERVER)

repository = resolveOption(args.repository, 'repository', DEFAULT_REPOSITORY)
links = resolveOption(args.links, 'followlinks', DEFAULT_LINKS)
mode = 'sandbox' if args.sandbox else 'repository'

# override repository if sandbox is chosen (sandbox name is the user name)
if args.sandbox:
    repository = args.user

# Check if server url path reachable
server = remove_trailing_slash(server)
if exists(server+'/pantheon'):
    logger.debug('server: %s is reachable', server)
else:
    sys.exit("server " + server + " is not reachable")

_info('Using server: ' + server)
_info('Using ' + mode + ': ' + repository)
print('--------------')

titleGlobs = config['titles'] if config is not None and 'titles' in config else ()
moduleGlobs = config['modules'] if config is not None and 'modules' in config else ()
resourceGlobs = config['resources'] if config is not None and 'resources' in config else '*'
unspecified_files = []
processed_files = []
logger.debug('titleGlobs: %s', titleGlobs)
logger.debug('moduleGlobs: %s', moduleGlobs)
logger.debug('resourceGlobs: %s', resourceGlobs)
logger.debug('args.directory: %s', args.directory)

resource_files = find_files(resourceGlobs, args.directory)
if resource_files:
    for f in resource_files:
        print("resource files matched: ", f)
        # Process files
        (status_code, reason) = process_file(f, "resources")

title_files = find_files(titleGlobs, args.directory)
if title_files:
    for f in title_files:
        print("title files matched: ", f)
        # Process files
        (status_code, reason) = process_file(f, "titles")

module_files = find_files(moduleGlobs, args.directory)
if module_files:
    logger.debug('module_files: %s', module_files)
    for f in module_files:
        print("module files matched: ", f)
        # Process files
        logger.debug('File path: %s', f)
        (status_code, reason) = process_file(f, "modules")

unspecified_files = get_unspecified_files(args.directory, processed_files, links)
if len(unspecified_files) > 0:
    num = len(unspecified_files)
    _warn(f'{num} additional files detected but not uploaded. Only files specified in ' + CONFIG_FILE +' are handled for upload.')
    # for file in unspecified_files:
    #     print(file)

print('Finished!')
