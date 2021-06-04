#!/usr/bin/python

import requests
from xml.dom import minidom

sitemaps = [
    'https://access.redhat.com/sitemap/assembly.sitemap.xml',
    'https://access.redhat.com/sitemap/module.sitemap.xml'
]

success = 0
failure = 0

for sitemap in sitemaps:
    r = requests.get(sitemap)

    if r.status_code != 200:
        print('Error, status code for ' + url + ' was ' + r.status_code)
    else:
        xmldoc = minidom.parseString(r.content)
        itemlist = xmldoc.getElementsByTagName('loc')
        print('\nProcessing ' + str(len(itemlist)) + ' items from ' + sitemap + '\n')
        for item in itemlist:
            try:
                rr = requests.get(item.firstChild.data)
                print(str(rr.status_code) + ': ' + rr.url)
                success += 1
            except requests.exceptions.RequestException as e:
                print(e)
                failure += 1

print('\nMade ' + str(success) + ' requests and raised ' + str(failure) + ' errors.')
