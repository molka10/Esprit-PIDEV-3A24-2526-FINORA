import re

def clean_admin_base():
    with open('templates/admin_base.html.twig', 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Remove Top Search
    content = re.sub(r'<!-- Top search START -->.*?<!-- Top search END -->', '', content, flags=re.DOTALL)

    # 2. Remove Notification dropdown
    content = re.sub(r'<!-- Notification dropdown START -->.*?<!-- Notification dropdown END -->', '', content, flags=re.DOTALL)

    # 3. Clean Profile dropdown links
    profile_links_pattern = r'<!-- Links -->.*?<!-- Dark mode options START -->'
    replacement = '<!-- Links -->\n\t\t\t\t\t\t<li> <hr class="dropdown-divider"></li>\n\t\t\t\t\t\t<!-- Dark mode options START -->'
    content = re.sub(profile_links_pattern, replacement, content, flags=re.DOTALL)

    with open('templates/admin_base.html.twig', 'w', encoding='utf-8') as f:
        f.write(content)

clean_admin_base()
print('Cleaned admin_base.html.twig')
