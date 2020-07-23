#!/usr/bin/python3
import argparse
import base64
import getpass
import json
import logging
import os
import re
import sys
from pathlib import PurePath

import requests
import yaml
from requests import Response
from datetime import datetime

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
repository: pantheonSampleRepo
variants:
    - path: path/to/attribute.adoc
      name: my_name
      canonical: true
    - path: path/to/attribute2.adoc
      name: my_name2

assemblies:
      - assemblies/*.adoc
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
    isAssembly = True if filetype == 'assemblies' else False

    content_root = 'sandbox' if args.sandbox else 'repositories'
    url = server + '/content/' + content_root + '/' + repository + '/entities'

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
            # print the response content received from Pantheon, not just reason
            _print_response('module', path, r.status_code, r.text)
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
                # print the response content received from Pantheon, not just reason
                _print_response('symlink', path, r.status_code, r.text)

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
                # print the response content received from Pantheon, not just reason
                _print_response('resource', path, r.status_code, r.text)
    elif isAssembly:
        url += '/' + path.name
        logger.debug('url: %s', url)
        jcr_primary_type = 'pant:assembly'
        data = _generate_data(jcr_primary_type, base_name, path.name, asccidoc_type='nt:file')
        # This is needed to add a new module version, otherwise it won't be handled
        data[':operation'] = 'pant:newAssemblyVersion'
        files = {'asciidoc': ('asciidoc', open(path, 'rb'), 'text/x-asciidoc')}

        if not args.dry:
            r = requests.post(url, headers=HEADERS, data=data, files=files, auth=(args.user, pw))
            # print the response content received from Pantheon, not just reason
            _print_response('assembly', path, r.status_code, r.text)
    logger.debug('')


def process_workspace(path):
    """
    Set up module_variants for the repository.
    Parameter:
    path: string
    """
    content_root = 'sandbox' if args.sandbox else 'repositories'
    url = server + '/content/' + content_root + '/' + repository

    # Populate payload
    logger.debug('url: %s', url)
    workspace = {}
    workspace['jcr:primaryType'] = 'pant:workspace'
    workspace['jcr:lastModified'] = datetime.now().utcnow().strftime("%Y-%m-%dT%H:%m:%S") #SimpleDateFormat:yyyy-MM-dd'T'HH:mm:ss
    # Process variants. variants is a list of dictionaries

    data = {}
    if variants:
        validateVariants()
        for variant in variants:
            # Each variant is of type dictionary. Rename the keys to match ModuleVariantDefinition
            module_variants = {}
            module_variants['pant:canonical'] = 'false'
            for key, value in variant.items():
                if key.lower() == 'name':
                    module_variants['pant:name'] = value
                if key.lower() == 'path':
                    module_variants['pant:attributesFilePath'] = value
                if key.lower() == 'canonical':
                    module_variants['pant:canonical'] = value
            if len(variants) == 1:
                module_variants['pant:canonical'] = 'true'
            if 'pant:name' in module_variants:
                data[module_variants['pant:name']] = module_variants
            # createVariant(data, path, url, workspace, isCanon)

    else:
        data = {'DEFAULT': {}}
    createVariant(data, path, url, workspace)


"""
Method to validate variants attributes
"""
def validateVariants():
    isCanon = False
    isCannonicalList = []
    variantNameList = []
    variantPathList = []
    for variant in variants:
        if 'name' not in variant or variant['name'] is None:  # name is mandatory for variant, throw errors in case of missing
            sys.exit("Variant (name) missing, please correct variant name ")
        if 'path' not in variant or  variant['path'] is None:  # path is mandatory for variant, throw errors in case of missing
            sys.exit("Variant (path) missing, please correct variant path ")
        if 'canonical' in variant:
            if variant['canonical'] is not None:
                isCannonicalList.append(variant['canonical'])
            else:
                sys.exit("Cannonical (Value) missing, please correct Cannonical value for "+variant['name'])
    for value in isCannonicalList:
        if type(value) == bool:
            if (not value):
                continue
            elif (not isCanon and value):
                isCanon = True
            else:
                sys.exit('Multiple Canonical attribute present, Only one variant can be Cannonical')
        else:
            sys.exit('Canonical Attribute takes only boolean values.')
    if len(variants) > 1 and not isCanon:
        sys.exit('Canonical attribute missing, Should be present in case multiple variants')


def createVariant(data, path, url, workspace):
    payload = {}
    payload[':content'] = json.dumps(data)  # '{"sample":"test"}'
    payload[':contentType'] = 'json'
    payload[':operation'] = 'import'
    payload[':replace'] = True
    # print(payload)
    if not args.dry:
        r: Response = requests.post(url, headers=HEADERS, data=workspace, auth=(args.user, pw))
        _print_response('workspace', path, r.status_code, r.reason)
        if r.status_code == 200 or r.status_code == 201:
            url = url + '/' + 'module_variants'
            r: Response = requests.post(url, headers=HEADERS, data=payload, auth=(args.user, pw))
            _print_response('module_variants', list(data.keys()), r.status_code, r.reason)
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
    if config is not None and keyword is not None and keyword in config:
        globs = config[keyword]
        logger.debug('keyword: $s', keyword)
        logger.debug('config[keyword] $s', config[keyword])
    else:
        globs = []
    if globs is not None:
        for i, val in enumerate(globs):
            globs[i] = val.replace('*', '[^/]+')
            logger.debug('key:val => $s : $s', i, val)

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


def process_attributes_as_resources(variants):
    resources = []
    for variant in variants:
        # Each variant is of type dictionary
        for key, value in variant.items():
            if key == 'path':
                resources.append(value)
    return resources


server = resolveOption(args.server, 'server', DEFAULT_SERVER)
# Check if server url path reachable
server = remove_trailing_slash(server)
if exists(server + '/pantheon'):
    logger.debug('server: %s is reachable', server)
else:
    sys.exit('server ' + server + ' is not reachable')

_info('Using server: ' + server)

if len(config.keys()) > 0 and 'repository' in config:
    # for repo_list in config['repositories']:
    repository = resolveOption(args.repository, '', config['repository'])
    # Enforce a repository being set in the pantheon.yml
    if repository == "" and mode == 'repository':
        sys.exit('repository is not set')

    mode = 'sandbox' if args.sandbox else 'repository'
    # override repository if sandbox is chosen (sandbox name is the user name)
    if args.sandbox:
        repository = args.user

    if 'variants' in config:
        variants = config['variants']
    else:
        variants = []
    _info('Using ' + mode + ': ' + repository)
    print('--------------')

    process_workspace(repository)
    attribute_files = []
    if variants:
        attribute_files = process_attributes_as_resources(variants)

    moduleGlobs = readYamlGlob(config, 'modules')
    resourceGlobs = readYamlGlob(config, 'resources')
    assemblyGlobs = readYamlGlob(config, 'assemblies')
    if attribute_files:
        if resourceGlobs is None:
            resourceGlobs = attribute_files
        else:
            resourceGlobs = resourceGlobs + attribute_files
    non_resource_files = []
    logger.debug('assemblyGlobs: %s', assemblyGlobs)
    logger.debug('moduleGlobs: %s', moduleGlobs)
    logger.debug('resourceGlobs: %s', resourceGlobs)
    logger.debug('args.directory: %s', args.directory)

    # List all files in the directory
    allFiles = []
    listdir_recursive(args.directory, allFiles)

    processRegexMatches(allFiles, resourceGlobs, 'resources')
    processRegexMatches(allFiles, moduleGlobs, 'modules')
    processRegexMatches(allFiles, assemblyGlobs, 'assemblies')

    leftoverFiles = len(allFiles)
    if leftoverFiles > 0:
        _warn(f'{leftoverFiles} additional files detected but not uploaded. Only files specified in '
              + CONFIG_FILE
              + ' are handled for upload.')

else:
    sys.exit('Modules and resources not found, please check yaml syntax')
print('Finished!')
