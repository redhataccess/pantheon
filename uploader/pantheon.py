#!/usr/bin/python3
import argparse
import base64
import getpass
import logging
import os
import re
import sys
from pathlib import PurePath

import requests
import yaml
from requests import Response
from pprint import pprint
DEFAULT_SERVER = 'http://localhost:8080'
DEFAULT_USER = 'author'
DEFAULT_PASSWORD = base64.b64decode(b'YXV0aG9y').decode()
CONFIG_FILE = 'pantheon2.yml'

HEADERS = {'cache-control': 'no-cache',
           'Accept': 'application/json'}


def _generate_data(jcr_primary_type, base_name, path_name, asccidoc_type):
    """
    Generate the data object for the API call.
    """
    data = {}
    if jcr_primary_type:
        data['jcr:primaryType'] = jcr_primary_type
    if base_name:
        data['jcr:title'] = base_name
        data['jcr:description'] = base_name
    if path_name:
        data['pant:originalName'] = path_name
    if asccidoc_type:
        data['asciidoc@TypeHint'] = asccidoc_type

    return data


def _info(message, colored=True):
    """
    Print an info message on the console. Warning messages are cyan
    """
    if colored:
        print('\033[96m{}\033[00m'.format(message))
    else:
        print(message)


def _warn(message, colored=True):
    """
    Print a warning message on the console. Warning messages are yellow
    """
    if colored:
        print('\033[93m{}\033[00m'.format(message))
    else:
        print(message)


def _error(message, colored=True):
    """
    Print an error message on the console. Warning messages are red
    """
    if colored:
        print('\033[91m{}\033[00m'.format(message))
    else:
        print(message)


def _print_response(filetype, path, response_code, reason):
    """
    Prints an http response in the appropriate terminal color
    """
    if 200 <= response_code < 300:
        _info(filetype + ': ' + str(path), False)
        _info(str(response_code) + ' ' + reason, True)
    elif response_code >= 500:
        _error(filetype + ': ' + str(path), True)
        _error(str(response_code) + ' ' + reason, True)
    else:
        print(response_code, reason)


parser = argparse.ArgumentParser(formatter_class=argparse.RawTextHelpFormatter, description='''\
Red Hat bulk upload module for Pantheon 2. This tool will scan a directory recursively and upload relevant files.

Both this uploader and Pantheon 2 are ALPHA software and features may update or change over time.

''')
parser.add_argument('push', nargs='+', help='Type of operation, default push')
parser.add_argument('--server', '-s', help='The Pantheon server to upload modules to, default ' + DEFAULT_SERVER)
parser.add_argument('--repository', '-r', help='The name of the Pantheon repository')
parser.add_argument('--attrFile', '-f', help='Path to the attribute File', dest='attrFile')
parser.add_argument('--user', '-u', help='Username for authentication, default \'' + DEFAULT_USER + '\'',
                    default=DEFAULT_USER)
parser.add_argument('--password', '-p',
                    help='Password for authentication, default \'' + DEFAULT_PASSWORD + '\'. If \'-\' is supplied, the script will prompt for the password.',
                    default=DEFAULT_PASSWORD)
parser.add_argument('--directory', '-d',
                    help='Directory to upload, default is current working directory. (' + os.getcwd() + ')',
                    default=os.getcwd())
parser.add_argument('--verbose', '-v', help='Print information that may be helpful for debugging', action='store_const',
                    const=True)
parser.add_argument('--dry', '-D',
                    help='Dry run; print information about what would be uploaded, but don\'t actually upload',
                    action='store_const', const=True)
parser.add_argument('--sandbox', '-b',
                    help='Push to the user\'s personal sandbox. This parameter overrides --repository',
                    action='store_const', const=True)
parser.add_argument('--sample', '-S',
                    help='Print a sample pantheon2.yml file to stdout (which you may want to redirect to a file).',
                    action='version', version='''\
# Config file for Pantheon v2 uploader
## server: Pantheon server URL
## repository: a unique name, which is visible in the user facing URL

## Note: Due to yaml syntax, any filepaths that start with a wildcard must be surrounded in quotes like so:
# modules:
#  - '*.adoc'

server: http://localhost:8080
repositories:
  - name: pantheonSampleRepo
    attributes: path/to/attribute.adoc

    modules:
      - master.adoc
      - modules/*.adoc

    resources:
      - shared/legal.adoc
      - shared/foreword.adoc
      - resources/*
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
    raise ValueError('Directory not found {}'.format(args.directory))

try:
    config = yaml.safe_load(open(args.directory + '/' + CONFIG_FILE))

except FileNotFoundError:
    logger.warning(
        'Could not find a valid config file(' + CONFIG_FILE + ') in this directory; all files will be treated as resource uploads.')
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


def process_file(path, filetype):
    """
    Processes the matched files and upload to pantheon through sling api call

    Paramters:
    path (string): A file path
    filetype (string): A type of file(assemblies [someday], modules or resources)

    Returns:
    list: It returns a list with value of the API call status_code and reason
    """
    isModule = True if filetype == 'modules' else False
    isResource = True if filetype == 'resources' else False
    content_root = 'sandbox' if args.sandbox else 'repositories'
    url = server + '/content/' + content_root + '/' + repository

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
        return

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
        url += '/' + path.name
        logger.debug('url: %s', url)
        jcr_primary_type = 'pant:module'
        data = _generate_data(jcr_primary_type, base_name, path.name, asccidoc_type='nt:file')
        # This is needed to add a new module version, otherwise it won't be handled
        data[':operation'] = 'pant:newModuleVersion'
        files = {'asciidoc': ('asciidoc', open(path, 'rb'), 'text/x-asciidoc')}

        # Minor question: which is correct, text/asciidoc or text/x-asciidoc?
        # It is text/x-asciidoc. Here's why:
        # https://tools.ietf.org/html/rfc2045#section-6.3
        # Paraphrased: "If it's not an IANA standard, use the 'x-' prefix."
        # Here's the list of standards; text/asciidoc isn't in it.
        # https://www.iana.org/assignments/media-types/media-types.xhtml#text

        if not args.dry:
            r = requests.post(url, headers=HEADERS, data=data, files=files, auth=(args.user, pw))
            _print_response('module', path, r.status_code, r.reason)
    elif isResource:
        if os.path.islink(path):
            target = str(os.readlink(path))
            url += '/' + path.name
            logger.debug('url: %s', url)
            if target[0] == '/':
                _error('Absolute symlink paths are unsupported: ' + str(path) + ' -> ' + target)
            elif not args.dry:
                symlinkData = {}
                symlinkData['jcr:primaryType'] = 'pant:symlink'
                symlinkData['pant:target'] = target
                r = requests.post(url, headers=HEADERS, data=symlinkData, auth=(args.user, pw))
                _print_response('symlink', path, r.status_code, r.reason)

        else:
            # determine the file content type, for some common ones
            file_type = None
            if path.suffix in ['.adoc', '.asciidoc']:
                file_type = 'text/x-asciidoc'
            # Upload as a regular file(nt:file)
            logger.debug('url: %s', url)
            files = {path.name: (path.name, open(path, 'rb'), file_type)}
            if not args.dry:
                r = requests.post(url, headers=HEADERS, files=files, auth=(args.user, pw))
                _print_response('resource', path, r.status_code, r.reason)
    logger.debug('')


def process_workspace(path):
    """
    Adds pant:attributeFile to the repository node.
    Parameter:
    path: string
    """
    content_root = 'sandbox' if args.sandbox else 'repositories'
    url = server + '/content/' + content_root + '/' + repository

    # Specify attributeFile property
    logger.debug('url: %s', url)
    data = {}
    data['jcr:primaryType'] = 'pant:workspace'
    if attributeFile:
        data['pant:attributeFile'] = attributeFile
    if not args.dry:
        r: Response = requests.post(url, headers=HEADERS, data=data, auth=(args.user, pw))
        _print_response('workspace', path, r.status_code, r.reason)
    logger.debug('')


def listdir_recursive(directory, allFiles):
    for name in os.listdir(directory):
        if name == 'pantheon2.yml' or name[0] == '.':
            continue
        path = PurePath(str(directory) + '/' + name)
        if os.path.isdir(path) and not os.path.islink(path):
            listdir_recursive(path, allFiles)
        else:
            allFiles.append(path)


def readYamlGlob(config, keyword):
    globs = config[keyword] if config is not None and keyword in config else ()
    # logger.debug('keyword: $s', keyword)
    # logger.debug('config[keyword] $s', config[keyword])
    if globs is not None:
        for i, val in enumerate(globs):
            globs[i] = val.replace('*', '[^/]+')
            # logger.debug('key:val => $s : $s', i, val)

    return globs


def processRegexMatches(files, globs, filetype):
    matches = []
    logger.debug(' === ' + filetype)
    for f in files:
        if os.path.islink(f):
            logger.debug(f)
            logger.debug(' -- is symlink')
            matches.append(f)
            process_file(f, filetype)
        else:
            subpath = str(f)[len(args.directory) + 1:]
            logger.debug(' Evaluating ' + subpath)
            for regex in globs or []:
                if re.match(regex, subpath):
                    logger.debug(' -- match ' + filetype + ' ' + regex)
                    matches.append(f)
                    process_file(f, filetype)
                    break  # necessary because the same file could potentially match more than 1 wildcard
    for f in matches:
        files.remove(f)


server = resolveOption(args.server, 'server', DEFAULT_SERVER)
# Check if server url path reachable
server = remove_trailing_slash(server)
if exists(server + '/pantheon'):
    logger.debug('server: %s is reachable', server)
else:
    sys.exit('server ' + server + ' is not reachable')

_info('Using server: ' + server)

if len(config.keys()) > 0 and 'repositories' in config:
    for repo_list in config['repositories']:
        repository = resolveOption(args.repository, '', repo_list['name'])
        # Enforce a repository being set in the pantheon.yml
        if repository == "" and mode == 'repository':
            sys.exit('repository is not set')

        mode = 'sandbox' if args.sandbox else 'repository'
        # override repository if sandbox is chosen (sandbox name is the user name)
        if args.sandbox:
            repository = args.user

        if 'attributes' in repo_list:
            attributeFile = resolveOption(args.attrFile, '', repo_list['attributes'])
        else:
            attributeFile = resolveOption(args.attrFile, '', '')

        if args.attrFile:
            if not os.path.isfile(args.directory + '/' + args.attrFile):
                sys.exit('attributes: ' + args.directory + '/' + args.attrFile + ' does not exist.')
        elif attributeFile:
            if args.directory:
                if not os.path.isfile(args.directory + '/' + attributeFile.strip()):
                    sys.exit('attributes2: ' + args.directory + '/' + attributeFile + ' does not exist.')
            else:
                if not os.path.isfile(attributeFile.strip()):
                    sys.exit('attributes3: ' + attributeFile + ' does not exist.')

        _info('Using ' + mode + ': ' + repository)
        _info('Using attributes: ' + attributeFile)
        print('--------------')


        process_workspace(repository)

        moduleGlobs = readYamlGlob(repo_list, 'modules')
        resourceGlobs = readYamlGlob(repo_list, 'resources')

        if attributeFile:
            if resourceGlobs == None:
                resourceGlobs = [attributeFile]
            else:
                resourceGlobs = resourceGlobs + [attributeFile]
        non_resource_files = []
        logger.debug('moduleGlobs: %s', moduleGlobs)
        logger.debug('resourceGlobs: %s', resourceGlobs)
        logger.debug('args.directory: %s', args.directory)


        # List all files in the directory
        allFiles = []
        listdir_recursive(args.directory, allFiles)

        processRegexMatches(allFiles, resourceGlobs, 'resources')
        processRegexMatches(allFiles, moduleGlobs, 'modules')

        leftoverFiles = len(allFiles)
        if leftoverFiles > 0:
            _warn(f'{leftoverFiles} additional files detected but not uploaded. Only files specified in '
                + CONFIG_FILE
                + ' are handled for upload.')

print('Finished!')
