
with open(r'c:\Users\ASUS\Downloads\integration complete\templates\base.html.twig', 'r', encoding='utf-8') as f:
    content = f.read()

ifs = content.count('{% if')
endifs = content.count('{% endif')
fors = content.count('{% for')
endfors = content.count('{% endfor')
blocks = content.count('{% block')
endblocks = content.count('{% endblock')

print(f"IFs: {ifs}, ENDIFs: {endifs}")
print(f"FORs: {fors}, ENDFORs: {endfors}")
print(f"BLOCKs: {blocks}, ENDBLOCKs: {endblocks}")
