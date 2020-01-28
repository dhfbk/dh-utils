from requests import get
from requests.exceptions import RequestException
from contextlib import closing
from bs4 import BeautifulSoup
import time
import argparse
import re


def simple_get(url):
    """
    Attempts to get the content at `url` by making an HTTP GET request.
    If the content-type of response is some kind of HTML/XML, return the
    text content, otherwise return None.
    """
    time.sleep(.3)
    try:
        with closing(get(url, stream=True)) as resp:
            if is_good_response(resp):
                return resp.content
            else:
                return None

    except RequestException as e:
        log_error('Error during requests to {0} : {1}'.format(url, str(e)))
        return None


def is_good_response(resp):
    """
    Returns True if the response seems to be HTML, False otherwise.
    """
    content_type = resp.headers['Content-Type'].lower()
    return (resp.status_code == 200
            and content_type is not None
            and content_type.find('html') > -1)


def log_error(e):
    """
    It is always a good idea to log errors.
    This function just prints them, but you can
    make it do anything.
    """
    print(e)


parser = argparse.ArgumentParser(description='Download Latin names from the web.')
parser.add_argument('output', help='the output file')

args = parser.parse_args()
# print(args.output)

f = open(args.output, "w")
conversion = {}

for code in range(ord('a'), ord('z') + 1):
    letter = chr(code)
    if letter in ("x", "y"):
        continue
    print("[INFO] Letter " + letter)
    addr = 'http://rbms.info/lpn/' + letter + '/'
    # print(addr)
    raw_html = simple_get(addr)
    html = BeautifulSoup(raw_html, 'html.parser')
    for a in html.select(".entry-content td a"):
        try:
            link = a['href']
            if "#" in link:
                continue
            if not "http://" in link:
                continue
            # print(link)
            name_td = ""
            try:
                name_td = a.parent.parent.select("td:nth-of-type(2)")[0]
            except IndexError:
                continue
            name_links = name_td.select("a")
            modern_name = a.parent.parent.select("td:nth-of-type(2)")[0].getText()
            if len(name_links) > 0:
                modern_name = name_links[0].getText()
            modern_name = modern_name.replace("\n", " ");
            modern_name = re.sub("\\[.*\\]", "", modern_name)

            # print(modern_name)

            if not modern_name:
                print("[ERR] Missing vernacular name for " + link)
                continue

            page_raw_html = simple_get(link)
            page_html = BeautifulSoup(page_raw_html, 'html.parser')
            for p in page_html.select(".entry-content p"):
                text = p.text
                text = text.replace("\n", " ")
                m = re.search("\\((.*?)=", text)
                if m is None:
                    continue
                parts = m.group(1).split(",")
                parts = [part.strip() for part in parts]
                for part in parts:
                    m = re.search("[a-zA-Z]", part)
                    if m is None:
                        print("[ERR] No letters in " + part)
                        continue
                    part = re.sub("\\[.*\\]", "", part)

                    f.write(part)
                    v_parts = modern_name.split("=")
                    for v_part in v_parts:
                        v_part = v_part.strip()
                        f.write("\t")
                        f.write(v_part)
                    f.write("\n")
                # print(parts)
        except KeyError:
            pass
    # for td in html.select(".entry-content tr td:first-child"):
    #     text = str(td)
    #     text = text.replace(">see also", ">")
    #     text = text.replace("> see also", ">")
    #     text = text.replace(">see ", ">")
    #     text = text.replace("> see ", ">")
    #     text = text.replace("*", "")
    #     parts = text.split("<br/>")
    #     for part in parts:
    #         clean = BeautifulSoup(part, "lxml").text.strip()
    #         f.write(clean)
    #         f.write("\n")

f.close()
