#!/usr/bin/python3

import requests
from xml.dom import minidom

sitemaps = [
    'https://access.redhat.com/sitemap/assembly.sitemap.xml',
    'https://access.redhat.com/sitemap/module.sitemap.xml'
]

def gather_urls():
    urls = []
    for sitemap in sitemaps:
        r = requests.get(sitemap)
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
            rr = requests.get(url)
            print(str(rr.status_code) + ': ' + rr.url)
            success += 1
        except requests.exceptions.RequestException as e:
            print(e)
            failure += 1
    return [success, failure]


def main():
    urls = gather_urls()
    q = perform_requests(urls)
    successes = q[0]
    failures = q[1]

    print('\nMade ' + str(successes) + ' requests and raised ' + str(failures) + ' errors.')


if __name__ == '__main__':
    main()
