#!/usr/bin/python3

import requests
from xml.dom import minidom
import sys

env = dict({
        "prod" : [
            'https://access.redhat.com/sitemap/assembly.sitemap.xml',
            'https://access.redhat.com/sitemap/module.sitemap.xml'
        ],
        "qa" : [
            'https://pantheon.corp.qa.redhat.com/api/sitemap/assembly.sitemap.xml',
            'https://pantheon.corp.qa.redhat.com/api/sitemap/module.sitemap.xml'
        ],
        "stage" : [
            'https://pantheon.corp.stage.redhat.com/api/sitemap/assembly.sitemap.xml',
            'https://pantheon.corp.stage.redhat.com/api/sitemap/module.sitemap.xml'
        ],
        "dev" : [
            'https://pantheon.corp.dev.redhat.com/api/sitemap/assembly.sitemap.xml',
            'https://pantheon.corp.dev.redhat.com/api/sitemap/module.sitemap.xml'
        ]})

headers = {'user-agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36'}

def gather_urls(user_input):
    urls = []
    sitemaps = env.get(user_input)
    for sitemap in sitemaps:
        r = requests.get(sitemap, headers=headers)
        if r.status_code != 200:
            print('Error, status code for ' + sitemap + ' was ' + r.status_code)
        else:
            xmldoc = minidom.parseString(r.content)
            itemlist = xmldoc.getElementsByTagName('loc')
            for item in itemlist:
                urls.append(item.firstChild.data)
    return urls


def perform_requests(urls):
    success = 0
    failure = 0
    for url in urls:
        try:
            rr = requests.get(url, headers=headers)
            print(str(rr.status_code) + ': ' + url)
            success += 1
        except requests.exceptions.RequestException as e:
            print(e)
            failure += 1
    return [success, failure]


def main(user_input):
    urls = gather_urls(user_input)
    q = perform_requests(urls)
    successes = q[0]
    failures = q[1]

    print('\nMade ' + str(successes) + ' requests and raised ' + str(failures) + ' errors.')


def print_usage():
    print(50 * "*")
    print(50 * "*")
    print("\nMissing argument : Expected dev / qa / stage / prod\n")
    print("Note : Purpose of script is parse cache on customer portal \n "
          "and request the url, to check, if it is live \n")
    print(50 * "*")
    print(50 * "*")


if __name__ == '__main__':
    try:
        args = sys.argv[1]
        main(args)
    except IndexError:
        print_usage()
        sys.exit(0)

