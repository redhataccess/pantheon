from json import JSONEncoder

import giturlparse
from git import Repo, RemoteProgress
import string
import random
import os
from .messaging import broker
import logging

logger = logging.getLogger(__name__)


class GitHelper():
    @classmethod
    def clone(cls, repo_url, clone_dir, branch, ssl_verify=False):
        """
        Clones the repo and the specified branch
        :param repo_url: URL of the git repo
        :param clone_dir: temporary directory where to clone to
        :param branch:
        :param ssl_verify: should the cert of git remote be verified
        :return:
        """
        config = 'http.sslVerify=false' if not ssl_verify else ''
        return Repo.clone_from(url=repo_url, to_path=clone_dir, branch=branch, progress=ProgressHelper(),
                               config=config)

    @classmethod
    def parse_git_url(cls, url):
        """
        Parses the git url into scheme, port, repo name etc.
        :param url: git url to be parsed
        :return:
        """
        return giturlparse.parse(url)


class ProgressHelper(RemoteProgress):
    def line_dropped(self, line):
        logger.info(line)

    def update(self, *args):
        logger.info(self._cur_line)


class FileHelper():
    @classmethod
    def get_random_name(cls, no_of_digits):
        """
        Generate a name containing random sequence of alphanumeric characters
        :param no_of_digits: length of the string to be generated
        :return:
        """
        return ''.join(random.choices(string.ascii_lowercase + string.digits, k=no_of_digits))


class MessageHelper():
    @classmethod
    def publish(cls, key, message):
        """
        Publish the key/message to redis
        :param key:
        :param message:
        :return:
        """
        try:
            logger.info("Publishing key="+key+" with message="+message)
            broker.set(key, message)
        except Exception as e:
            logger.error('Could not publish state due to'+str(e))

    @classmethod
    def unpublish(cls, key):
        broker.delete(key)


class JsonEncoder(JSONEncoder):
    """
    Helper for encoding objects into JSON
    """
    def default(self, object_to_serialize):
        return object_to_serialize.__dict__