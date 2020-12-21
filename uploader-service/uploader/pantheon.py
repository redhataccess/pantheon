#!/usr/bin/python3
import argparse
import base64
import getpass
import json
import logging
import os
import re
import sys
import uuid
from collections import namedtuple, defaultdict
from pathlib import PurePath
import requests
import yaml
from requests import Response
from datetime import datetime
import base64
import logging
from collections import namedtuple

DEFAULT_SERVER = 'http://localhost:8080'
DEFAULT_USER = 'author'
DEFAULT_PASSWORD = base64.b64decode(b'YXV0aG9y').decode()
CONFIG_FILE = 'pantheon2.yml'

HEADERS = {'cache-control': 'no-cache',
           'Accept': 'application/json'}
logger = logging.getLogger(__name__)

broker = None
channel_name = 'default'
ProcessedStatus = namedtuple('ProcessedStatus', ['path', 'response_code', 'response_details'])


class Data:
    def __init__(self):
        self.processed_data = {
            'modules': [],
            'modules_not_processed': [],
            'assemblies': [],
            'assemblies_not_processed': [],
            'resources': [],
            'resources_not_processed': [],
            'server': {},
            'module_variants': [],
            'other_status': [],
            'current_status': 'processing',
            'type_processing': '',
        }


def reset_processed_data(data):
    data.processed_data.clear()


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
        logger.info(message)


def _warn(message, colored=True):
    """
    Print a warning message on the console. Warning messages are yellow
    """
    if colored:
        print('\033[93m{}\033[00m'.format(message))
    else:
        print(message)
        logger.warn(message)


def _error(message, colored=True):
    """
    Print an error message on the console. Warning messages are red
    """
    if colored:
        print('\033[91m{}\033[00m'.format(message))
    else:
        print(message)
        logger.error(message)


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


def resolveOption(parserVal, configKey, default, config):
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


def process_file(path, filetype, server, sandbox, repository, directory, dry, user, pw, should_publish, status_data:Data):
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

    content_root = 'sandbox' if sandbox else 'repositories'
    url = server + '/content/' + content_root + '/' + repository + '/entities'

    path = PurePath(path)
    base_name = path.stem

    ppath = path
    hiddenFolder = False
    while not ppath == PurePath(directory):
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
    parent_dir_str = str(path.parent.relative_to(directory))
    if parent_dir_str == '.':
        parent_dir_str = ''
    logger.debug('parent_dir_str: %s', parent_dir_str)
    # file becomes a/file/name (no extension)

    if parent_dir_str:
        url += '/' + parent_dir_str

    logger.debug('base name: %s', base_name)

    # Asciidoc content (treat as a module)
    if isModule:
        status_data.processed_data['type_processing'] = 'module'
        process_module(base_name, dry, path, pw, url, user, status_data)
        publish_status(should_publish, status_data.processed_data)
    elif isResource:
        status_data.processed_data['type_processing'] = 'resource'
        process_resource(dry, path, pw, url, user, status_data)
        publish_status(should_publish, status_data.processed_data)
    elif isAssembly:
        status_data.processed_data['type_processing'] = 'assembly'
        process_assembly(base_name, dry, path, pw, url, user, status_data)
        publish_status(should_publish, status_data.processed_data)
    logger.debug('')


def process_assembly(base_name, dry, path, pw, url, user, status_data:Data):
    url += '/' + path.name
    logger.debug('url: %s', url)
    jcr_primary_type = 'pant:assembly'
    data = _generate_data(jcr_primary_type, base_name, path.name, asccidoc_type='nt:file')
    # This is needed to add a new module version, otherwise it won't be handled
    data[':operation'] = 'pant:newAssemblyVersion'
    files = {'asciidoc': ('asciidoc', open(path, 'rb'), 'text/x-asciidoc')}
    if not dry:
        r = requests.post(url, headers=HEADERS, data=data, files=files, auth=(user, pw))
        # print the response content received from Pantheon, not just reason
        if not 200 <= r.status_code < 300:
            status_data.processed_data['assemblies_not_processed'].append(create_status_data(path, r.status_code, r.text))
        else:
            status_data.processed_data['assemblies'].append(create_status_data(path, r.status_code, r.text))
        _print_response('assembly', path, r.status_code, r.text)


def publish_status(should_publish, processed_data):
    global broker
    if not should_publish:
        logger.info("not publishing the status details to the broker as broker is not being used")
        _info("not publishing the status details to the broker as broker is not being used")
        return
    data = json.dumps(processed_data)
    # broker.set(channel_name, data)
    print('publishing: '+data +' with key:'+channel_name)
    broker.set(channel_name, data)


def process_resource(dry, path, pw, url, user, status_data:Data):
    if os.path.islink(path):
        target = str(os.readlink(path))
        url += '/' + path.name
        logger.debug('url: %s', url)
        if target[0] == '/':
            _error('Absolute symlink paths are unsupported: ' + str(path) + ' -> ' + target)
        elif not dry:
            symlinkData = {}
            symlinkData['jcr:primaryType'] = 'pant:symlink'
            symlinkData['pant:target'] = target
            r = requests.post(url, headers=HEADERS, data=symlinkData, auth=(user, pw))
            # print the response content received from Pantheon, not just reason
            if not 200 <= r.status_code < 300:
                status_data.processed_data['resources_not_processed'].append(create_status_data(path, r.status_code, r.text))
            else:
                status_data.processed_data['resources'].append(create_status_data(path, r.status_code, r.text))
            _print_response('symlink', path, r.status_code, r.text)

    else:
        # determine the file content type, for some common ones
        file_type = None
        if path.suffix in ['.adoc', '.asciidoc']:
            file_type = 'text/x-asciidoc'
        # Upload as a regular file(nt:file)
        logger.debug('url: %s', url)
        files = {path.name: (path.name, open(path, 'rb'), file_type)}
        if not dry:
            r = requests.post(url, headers=HEADERS, files=files, auth=(user, pw))
            # print the response content received from Pantheon, not just reason
            if not 200 <= r.status_code < 300:
                status_data.processed_data['resources_not_processed'].append(create_status_data(path, r.status_code, r.text))
            else:
                status_data.processed_data['resources'].append(create_status_data(path, r.status_code, r.text))
            _print_response('resource', path, r.status_code, r.text)


def process_module(base_name, dry, path, pw, url, user, status_data:Data):
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
    if not dry:
        r = requests.post(url, headers=HEADERS, data=data, files=files, auth=(user, pw))
        if not 200 <= r.status_code < 300:
            status_data.processed_data['modules_not_processed'].append(create_status_data(path, r.status_code, r.text))
        else:
            status_data.processed_data['modules'].append(create_status_data(path, r.status_code, r.text))
        # print the response content received from Pantheon, not just reason
        _print_response('module', path, r.status_code, r.text)


def process_workspace(path, server, sandbox, repository, variants, user, pw, dry, status_data:Data):
    """
    Set up module_variants for the repository.
    Parameter:
    path: string
    """
    content_root = 'sandbox' if sandbox else 'repositories'
    url = server + '/content/' + content_root + '/' + repository

    # Populate payload
    logger.debug('url: %s', url)
    workspace = {}
    workspace['jcr:primaryType'] = 'pant:workspace'
    workspace['jcr:lastModified'] = datetime.now().utcnow().strftime(
        "%Y-%m-%dT%H:%m:%S")  # SimpleDateFormat:yyyy-MM-dd'T'HH:mm:ss
    # Process variants. variants is a list of dictionaries

    data = {}
    if variants:
        if not validateVariants(variants, status_data):
            return "Variants are not valid"

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
    return createVariant(data, path, url, workspace, user, pw, dry, status_data)


"""
Method to validate variants attributes
"""


def validateVariants(variants, status_data:Data):
    isCanon = False
    isCannonicalList = []
    variantNameList = []
    variantPathList = []
    for variant in variants:
        if 'name' not in variant or variant[
            'name'] is None:  # name is mandatory for variant, throw errors in case of missing
            status_data.processed_data['other_status'].append(
                create_status_data(variants, 400, "Variant (name) missing, please correct variant name "))
            return False
        if 'path' not in variant or variant[
            'path'] is None:  # path is mandatory for variant, throw errors in case of missing
            status_data.processed_data['other_status'].append(
                create_status_data(variants, 400, "Variant (path) missing, please correct variant path "))
            return False

        if 'canonical' in variant:
            if variant['canonical'] is not None:
                isCannonicalList.append(variant['canonical'])
            else:
                status_data.processed_data['other_status'].append(
                    create_status_data(variants, 400,
                                       "Cannonical (Value) missing, please correct Cannonical value for " + variant[
                                           'name']))
                return False

    for value in isCannonicalList:
        if type(value) == bool:
            if (not value):
                continue
            elif (not isCanon and value):
                isCanon = True
            else:
                status_data.processed_data['other_status'].append(
                    create_status_data(variants, 400,
                                       'Multiple Canonical attribute present, Only one variant can be Cannonical'))
                return False
        else:
            status_data.processed_data['other_status'].append(
                create_status_data(variants, 400,
                                   'Canonical Attribute takes only boolean values.'))
            return False
    if len(variants) > 1 and not isCanon:
        status_data.processed_data['other_status'].append(
            create_status_data(variants, 400,
                               'Canonical attribute missing, Should be present in case multiple variants'))
        return False
    return True


def createVariant(data, path, url, workspace, user, pw, dry, status_data:Data):
    payload = {}
    payload[':content'] = json.dumps(data)  # '{"sample":"test"}'
    payload[':contentType'] = 'json'
    payload[':operation'] = 'import'
    payload[':replace'] = True
    # print(payload)
    if not dry:
        r: Response = requests.post(url, headers=HEADERS, data=workspace, auth=(user, pw))
        _print_response('workspace', path, r.status_code, r.text)
        if r.status_code == 200 or r.status_code == 201:
            url = url + '/' + 'module_variants'

            r: Response = requests.post(url, headers=HEADERS, data=payload, auth=(user, pw))
            _print_response('module_variants', list(data.keys()), r.status_code, r.text)
            if r.status_code == 200 or r.status_code == 201:
                status_data.processed_data['module_variants'].append(create_status_data(path, r.status_code, r.text))
                # indicate that variant have been created
                return None
            else:
                # indicate that variant have not been created
                return r.text
        else:
            # indicate that workspace have not been created
            return r.text
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
        logger.debug('keyword: {0}'.format(str(keyword)))
        logger.debug('config[keyword] {0}'.format(str(config[keyword])))
    else:
        globs = []
    if globs is not None:
        for i, val in enumerate(globs):
            globs[i] = val.replace('*', '[^/]+')
            logger.debug('key:val => {0} : {1}'.format(str(i), str(val)))

    return globs


def processRegexMatches(files, globs, filetype, server, sandbox, repository, directory, dry, user, pw, should_publish, status_data:Data):
    matches = []
    logger.debug(' === ' + filetype)
    for f in files:
        if os.path.islink(f):
            logger.debug(f)
            logger.debug(' -- is symlink')
            matches.append(f)
            process_file(f, filetype, server, sandbox, repository, directory, dry, user, pw, should_publish, status_data)
        else:
            subpath = str(f)[len(directory) + 1:]
            logger.debug(' Evaluating ' + subpath)
            for regex in globs or []:
                if re.match(regex, subpath):
                    logger.debug(' -- match ' + filetype + ' ' + regex)
                    matches.append(f)
                    process_file(f, filetype, server, sandbox, repository, directory, dry, user, pw, should_publish, status_data)
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


def create_args():
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
    parser.add_argument('--verbose', '-v', help='Print information that may be helpful for debugging',
                        action='store_const',
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
    parser.add_argument('--use-broker',
                        help='Use a message broker to pass the processing updates to other applications. \n Currently '
                             'only local running redis is supported',
                        action='store_true', default=False
    )
    return parser.parse_args()


def main():
    # global pw, config
    args = create_args()
    user = args.user

    directory = args.directory if args.directory else os.getcwd()

    attrFile = args.attrFile

    dry = args.dry

    pw = args.password
    server = args.server
    logStr = 'DEBUG' if args.verbose is not None else 'WARNING'
    numeric_level = getattr(logging, logStr, None)
    repository = args.repository
    sandbox = args.sandbox
    use_broker = True if args.use_broker else False
    _info("Using user:" + user)
    _info("Using dry:" + dry)
    _info("Using server:" + server)
    _info("Using logStr:" + logStr)
    _info("Using numeric_level:" + numeric_level)
    _info("Using repository:" + repository)
    _info("Using directory:" + sandbox)
    _info("Using broker:" + use_broker)
    start_process(numeric_level, pw, directory, server, user, repository, sandbox, dry, attrFile, use_broker)

# ToDo: find a better way to handle variants validation
def get_status(err):
    return 400 if "valid" in err else 500


def setup_broker(channel):
    import redis
    global broker, channel_name
    broker = redis.Redis(decode_responses=True)
    broker.pubsub()
    # channel_name is global and hence can be set from outside
    channel_name = channel if channel_name == 'default' else channel_name


def start_process(numeric_level=30, pw=None, directory=None, server=DEFAULT_SERVER, user=None, repository=None,
                  sandbox=None
                  , dry=None, attrFile=None, use_broker=False):
    # initialize status update ds
    status_data = Data()
    set_logger(numeric_level)

    if pw == '-':
        pw = getpass.getpass()
    config = None
    if not os.path.exists(directory):
        raise ValueError('Directory not found {}'.format(directory))
    try:
        config = yaml.safe_load(open(directory + '/' + CONFIG_FILE))
    except FileNotFoundError:
        logger.warning(
            'Could not find a valid config file(' + CONFIG_FILE + ') in this directory; all files will be treated as resource uploads.')
    logger.debug('config: %s', config)

    repository = resolveOption(repository, '', config['repository'], config) if repository is None else repository

    if use_broker:
        setup_broker(repository)

    server = resolveOption(server, 'server', DEFAULT_SERVER, config)
    # Check if server url path reachable
    server = remove_trailing_slash(server)
    if exists(server + '/pantheon'):
        logger.debug('server: %s is reachable', server)
    else:
        status_data.processed_data['server'] = create_status_data(server, '503', 'server ' + server + ' is not reachable')
        publish_status(use_broker, status_data.processed_data)
        return False
    _info('Using server: ' + server)

    if len(config.keys()) > 0 and 'repository' in config:
        # for repo_list in config['repositories']:

        # Enforce a repository being set in the pantheon.yml
        if repository == "" and mode == 'repository':
            status_data.processed_data['other_status'].append(create_status_data("repository error", 400, 'repository is not set'))

        mode = 'sandbox' if sandbox else 'repository'
        # override repository if sandbox is chosen (sandbox name is the user name)
        if sandbox:
            repository = user

        if 'variants' in config:
            variants = config['variants']
        else:
            variants = []
        _info('Using ' + mode + ': ' + repository)
        print('--------------')
        err = process_workspace(repository, server, sandbox, repository, variants, user, pw, dry, status_data)
        if err is not None:
            status_data.processed_data['other_status'].append(create_status_data(repository, get_status(err),
                                                                     'Either workspace or variant could not be '
                                                                     'created because of {0}'.format(
                                                                         err)))
            logger.warning('Either workspace or variant could not be created because of {0}'.format(err))
            publish_status(use_broker, status_data.processed_data)
            status_data.processed_data['current_status']= "error"
            return False
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
        logger.debug('args.directory: %s', directory)

        # List all files in the directory
        allFiles = []
        listdir_recursive(directory, allFiles)

        processRegexMatches(allFiles, resourceGlobs, 'resources', server, sandbox, repository, directory, dry, user, pw, use_broker, status_data)
        processRegexMatches(allFiles, moduleGlobs, 'modules', server, sandbox, repository, directory, dry, user, pw, use_broker, status_data)
        processRegexMatches(allFiles, assemblyGlobs, 'assemblies', server, sandbox, repository, directory, dry, user,
                            pw, use_broker, status_data)

        leftoverFiles = len(allFiles)
        if leftoverFiles > 0:
            _warn(f'{leftoverFiles} additional files detected but not uploaded. Only files specified in '
                  + CONFIG_FILE
                  + ' are handled for upload.')

    else:
        status_data.processed_data['other_status'].append(
            create_status_data(repository, 400, 'Modules and resources not found, please check yaml syntax'))
        return False
    logger.info('Finished!')
    finalise_update(use_broker, status_data)
    return True


def finalise_update(use_broker, status_data:Data):
    status_data.processed_data['current_status'] = "done"
    status_data.processed_data['type_processing'] = "done"
    publish_status(use_broker, status_data.processed_data)


def set_logger(numeric_level):
    if not isinstance(numeric_level, int):
        raise ValueError('Invalid log level: %s' % numeric_level)
    logger.setLevel(numeric_level)
    logger.addHandler(logging.StreamHandler())


def create_status_data(path, status, details):
    return ProcessedStatus(str(path), status, details)._asdict()


if __name__ == '__main__':
    main()
