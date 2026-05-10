
import re

with open(r'c:\Users\ASUS\Downloads\integration complete\templates\base.html.twig', 'r', encoding='utf-8') as f:
    lines = f.readlines()

stack = []
for i, line in enumerate(lines):
    line_num = i + 1
    # Find all twig tags in this line
    tags = re.findall(r'{%[^%]*%}', line)
    for tag in tags:
        # Check for opening tags
        if '{% block' in tag:
            stack.append(('block', line_num, tag))
        elif '{% if' in tag:
            stack.append(('if', line_num, tag))
        elif '{% for' in tag:
            stack.append(('for', line_num, tag))
        elif '{% macro' in tag:
            stack.append(('macro', line_num, tag))
        
        # Check for closing tags
        elif '{% endblock' in tag:
            if not stack or stack[-1][0] != 'block':
                print(f"Error: unexpected {{% endblock %}} at line {line_num}")
            else:
                stack.pop()
        elif '{% endif' in tag:
            if not stack or stack[-1][0] != 'if':
                print(f"Error: unexpected {{% endif %}} at line {line_num}")
            else:
                stack.pop()
        elif '{% endfor' in tag:
            if not stack or stack[-1][0] != 'for':
                print(f"Error: unexpected {{% endfor %}} at line {line_num}")
            else:
                stack.pop()
        elif '{% endmacro' in tag:
            if not stack or stack[-1][0] != 'macro':
                print(f"Error: unexpected {{% endmacro %}} at line {line_num}")
            else:
                stack.pop()
        elif '{% end' in tag:
             # Generic endblock?
             if '{% endblock %}' in tag or '{% endblock %}' in tag:
                 pass # already handled
             else:
                 print(f"Warning: ambiguous end tag at line {line_num}: {tag}")

if stack:
    print("Unclosed tags:")
    for type, line_num, tag in stack:
        print(f"Line {line_num}: {tag}")
else:
    print("All tags are correctly nested.")
