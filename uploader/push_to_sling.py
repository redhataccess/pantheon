#!/usr/bin/python3
import os
import requests
import argparse
import getpass
import logging
import yaml
import socket
from pathlib import PurePath

DEFAULT_SERVER = 'http://localhost:8080'
DEFAULT_REPOSITORY = getpass.getuser() + '_' + socket.gethostname()
DEFAULT_USER = 'demo'
DEFAULT_PASSWORD = 'demo'
DEFAULT_LINKS = False

HEADERS = {'cache-control': 'no-cache',
           'Accept': 'application/json'}


def matches(path, globs, globType):
    for glob in globs:
        if path.match(glob):
            logger.debug('File %s matches on %s glob %s', file, globType, glob)
            return True
    return False


parser = argparse.ArgumentParser(formatter_class=argparse.RawTextHelpFormatter, description='''\
Red Hat bulk upload module for Pantheon 2. This tool will scan a directory recursively and upload relevant files.

Both this uploader and Pantheon 2 are ALPHA software and features may update or change over time. 
 
''')
parser.add_argument('--server', '-s', help='The Pantheon server to upload modules to, default ' + DEFAULT_SERVER)
parser.add_argument('--repository', '-r', help='The name of the Pantheon repository, default is username_hostname (' + DEFAULT_REPOSITORY + ')')
parser.add_argument('--user', '-u', help='Username for authentication, default \'' + DEFAULT_USER + '\'', default=DEFAULT_USER)
parser.add_argument('--password', '-p', help='Password for authentication, default \'' + DEFAULT_PASSWORD + '\'. If \'-\' is supplied, the script will prompt for the password.', default=DEFAULT_PASSWORD)
parser.add_argument('--directory', '-d', help='Directory to upload, default is current working directory. (' + os.getcwd() + ')', default=os.getcwd())
parser.add_argument('--links', '-l', help='Resolve symlinks when searching for files to upload', action='store_const', const=True)
parser.add_argument('--verbose', '-v', help='Print information that may be helpful for debugging', action='store_const', const=True)
parser.add_argument('--dry', '-D', help='Dry run; print information about what would be uploaded, but don\'t actually upload', action='store_const', const=True)
parser.add_argument('--sample', '-S', help='Print a sample pantheon2.yml file to stdout (which you may want to redirect to a file).', action='version', version='''\
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
try:
    config = yaml.safe_load(open(args.directory + '/pantheon2.yml'))
except FileNotFoundError:
    logger.warning('Could not find a valid config file in this directory; all files will be treated as resource uploads.')
logger.debug('config: %s', config)


def resolveOption(parserVal, configKey, default):
    if parserVal is not None:
        return parserVal
    elif config is not None and config[configKey] is not None:
        return config[configKey]
    else:
        return default


server = resolveOption(args.server, 'server', DEFAULT_SERVER)
repository = resolveOption(args.repository, 'repository', DEFAULT_REPOSITORY)
links = resolveOption(args.links, 'followlinks', DEFAULT_LINKS)

print('Using server: ' + server)
print('Using repository: ' + repository)
print('--------------')

titleGlobs = config['titles'] if config is not None else ()
moduleGlobs = config['modules'] if config is not None else ()
resourceGlobs = config['resources'] if config is not None else '*'

for root, dirs, files in os.walk(args.directory, followlinks=links):
    for file in files:
        if file == 'pantheon2.yml':
            continue
        logger.debug('root: %s', root)
        logger.debug('file: %s', file)
        path = PurePath(root + '/' + file)

        # These distinctions aren't important right now but they set us up for later
        isTitle = matches(path, titleGlobs, 'title')
        isModule = matches(path, moduleGlobs, 'module') if not isTitle else False
        isResource = matches(path, resourceGlobs, 'resource') if not isModule else False

        if isTitle or isModule or isResource:
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
                logger.info('Skipping %s because it is hidden.', str(path))
                logger.info('')
                continue

            # parent directory
            parent_dir_str = str(path.parent.relative_to(args.directory))
            if parent_dir_str == '.':
                parent_dir_str = ''
            logger.debug('parent_dir_str: %s', parent_dir_str)
            # file becomes a/file/name (no extension)

            url = server + "/content/repositories/" + repository
            if parent_dir_str:
                url += '/' + parent_dir_str

            logger.debug('base name: %s', base_name)

            # Asciidoc content (treat as a module)
            if path.suffix == '.adoc' or path.suffix == '.asciidoc':
                print(path)
                url += '/' + path.name
                logger.debug(url)
                data = {"jcr:primaryType": 'pant:module',
                        "jcr:title": base_name,
                        "jcr:description": base_name,
                        "sling:resourceType": 'pantheon/modules',
                        "pant:originalName": path.name,
                        "asciidoc@TypeHint": 'nt:file'}
                files = {'asciidoc': ('asciidoc', open(path, 'rb'), 'text/x-asciidoc')}

                # Minor question: which is correct, text/asciidoc or text/x-asciidoc?
                # It is text/x-asciidoc. Here's why:
                # https://tools.ietf.org/html/rfc2045#section-6.3
                # Paraphrased: "If it's not an IANA standard, use the 'x-' prefix.
                # Here's the list of standards; text/asciidoc isn't in it.
                # https://www.iana.org/assignments/media-types/media-types.xhtml#text

                if not args.dry:
                    r = requests.post(url, headers=HEADERS, data=data, files=files, auth=(args.user, pw))
                    print(r.status_code, r.reason)
                logger.debug('')
            # Otherwise just upload as a regular file
            else:
                print(path)
                logger.debug(url)
                files = {path.name: (path.name, open(path, 'rb'))}
                if not args.dry:
                    r = requests.post(url, headers=HEADERS, files=files, auth=(args.user, pw))
                    print(r.status_code, r.reason)
                logger.debug('')
        else:
            logger.debug('File %s does not match the given glob pattern(s).', file)
            logger.debug('')

print('Finished!')
