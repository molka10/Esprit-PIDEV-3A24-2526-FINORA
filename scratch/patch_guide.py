import re

path = r'c:\Users\ASUS\Downloads\integration complete\templates\base.html.twig'
with open(path, encoding='utf-8') as f:
    content = f.read()

old = '<div class="guide-overlay" id="guideOverlay" onclick="stopFinoraGuide()"></div>'
new = old + '\n<div id="guidePanel"></div>'

if old in content:
    content = content.replace(old, new, 1)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Done — guidePanel added")
else:
    print("Pattern not found")
