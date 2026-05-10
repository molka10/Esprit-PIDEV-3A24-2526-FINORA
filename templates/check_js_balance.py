
with open(r'c:\Users\ASUS\Downloads\integration complete\templates\base.html.twig', 'r', encoding='utf-8') as f:
    content = f.read()

# Only check the part I added
start_marker = "<!-- Unified Notification Bell JS -->"
if start_marker in content:
    js_part = content[content.find(start_marker):]
    
    # Simple balance check
    parens = js_part.count('(') - js_part.count(')')
    braces = js_part.count('{') - js_part.count('}')
    brackets = js_part.count('[') - js_part.count(']')
    
    print(f"Parens: {parens}, Braces: {braces}, Brackets: {brackets}")
    
    # More detailed check for braces
    stack = []
    for i, char in enumerate(js_part):
        if char == '{':
            stack.append(i)
        elif char == '}':
            if stack:
                stack.pop()
            else:
                print(f"Extra closing brace near: {js_part[i-20:i+20]}")
    if stack:
        for s in stack:
            print(f"Unclosed opening brace near: {js_part[s:s+50]}")
else:
    print("Marker not found")
