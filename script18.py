import os
import re

controllers = [
    'src/Controller/AppelOffreController.php',
    'src/Controller/CandidatureController.php',
    'src/Controller/CategorieController.php',
    'src/Controller/UserController.php'
]

for controller in controllers:
    if not os.path.exists(controller):
        continue
    with open(controller, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Fix the broken code
    # Replace (->query->get('role') with (->query->get('role')
    content = content.replace('(->query->get', '(->query->get')
    content = content.replace('->getSession', '->getSession')
    
    with open(controller, 'w', encoding='utf-8') as f:
        f.write(content)

print('Controllers fixed!')
