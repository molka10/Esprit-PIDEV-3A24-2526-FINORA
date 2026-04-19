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
    
    # Precise fix for the broken lines
    content = content.replace('if (->query->get', 'if (->query->get')
    content = content.replace('->getSession()->set', '->getSession()->set')
    
    with open(controller, 'w', encoding='utf-8') as f:
        f.write(content)

print('Controllers fixed properly!')
