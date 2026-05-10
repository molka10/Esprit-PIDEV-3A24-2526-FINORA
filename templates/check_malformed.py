
import re

with open(r'c:\Users\ASUS\Downloads\integration complete\templates\base.html.twig', 'r', encoding='utf-8') as f:
    content = f.read()

# Find all occurrences of {% and check if they have matching %}
tags = re.findall(r'{%[^%]*%}', content)
total_tags = content.count('{%')
if len(tags) != total_tags:
    print(f"Error: {total_tags} opening tags found, but only {len(tags)} correctly closed tags found.")
    
    # Find the problematic one
    starts = [m.start() for m in re.finditer(r'{%', content)]
    for s in starts:
        end = content.find('%}', s)
        if end == -1 or '\n' in content[s:end]: # assuming tags don't span multiple lines usually, or at least check if %} is missing
            print(f"Malformed tag near: {content[s:s+50]}")
else:
    print("All tags seem correctly formed with {% and %}")

# Also check {{ and }}
curlies = content.count('{{')
closed_curlies = content.count('}}')
if curlies != closed_curlies:
    print(f"Error: {curlies} '{{{{' found, but {closed_curlies} '}}}}' found.")
else:
    print("All curlies seem correctly balanced.")
