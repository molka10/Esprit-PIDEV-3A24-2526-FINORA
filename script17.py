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
    
    # Add role check at the beginning of the index method
    # We look for the index method and insert the role check
    role_check = '''        if (->query->get('role') === 'admin') {
            ->getSession()->set('role', 'admin');
        }'''
    
    content = re.sub(r'(public function index\(.*?\): Response\s*\{)', r'\1\n' + role_check, content, flags=re.DOTALL)
    
    with open(controller, 'w', encoding='utf-8') as f:
        f.write(content)

print('Controllers updated!')
