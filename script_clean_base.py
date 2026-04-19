import re

def clean_base():
    with open('templates/base.html.twig', 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Remove Nav Search
    content = re.sub(r'<!-- Nav Search START -->.*?<!-- Nav Search END -->', '', content, flags=re.DOTALL)

    # 2. Remove Wishlist
    content = re.sub(r'<!-- Wishlist START -->.*?<!-- Wishlist END -->', '', content, flags=re.DOTALL)

    # 3. Remove Notification dropdown
    content = re.sub(r'<!-- Notification dropdown START -->.*?<!-- Notification dropdown END -->', '', content, flags=re.DOTALL)

    # 4. Clean Profile dropdown links
    profile_links_pattern = r'<!-- Links -->.*?<!-- Dark mode options START -->'
    replacement = '<!-- Links -->\n\t\t\t\t\t\t<li> <hr class="dropdown-divider"></li>\n\t\t\t\t\t\t<!-- Dark mode options START -->'
    content = re.sub(profile_links_pattern, replacement, content, flags=re.DOTALL)

    # 5. Remove Category Nav link START ... END
    content = re.sub(r'<!-- Category Nav link START -->.*?<!-- Category Nav link END -->', '', content, flags=re.DOTALL)

    # 6. Remove <hr class="my-0"> if it's right before category nav
    content = re.sub(r'<hr class="my-0">\s*(?={% block body %})', '', content, flags=re.DOTALL)

    with open('templates/base.html.twig', 'w', encoding='utf-8') as f:
        f.write(content)

clean_base()
print('Cleaned base.html.twig')
