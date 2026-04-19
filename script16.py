import re

with open('templates/dashboard/admin.html.twig', 'r', encoding='utf-8') as f:
    content = f.read()

# Fix table header text colors by explicitly setting text-white on the h5 tags
content = content.replace('<h5 class="mb-0"><i class="fas fa-clock"></i> Derniers Appels d\\'Offre</h5>', '<h5 class="mb-0 text-white"><i class="fas fa-clock"></i> Derniers Appels d\\'Offre</h5>')
content = content.replace('<h5 class="mb-0"><i class="fas fa-clock"></i> Dernières Candidatures</h5>', '<h5 class="mb-0 text-white"><i class="fas fa-clock"></i> Dernières Candidatures</h5>')
content = content.replace('<h5 class="mb-0"><i class="fas fa-chart-pie"></i> Statut des Appels d\\'Offre</h5>', '<h5 class="mb-0 text-white"><i class="fas fa-chart-pie"></i> Statut des Appels d\\'Offre</h5>')
content = content.replace('<h5 class="mb-0"><i class="fas fa-chart-pie"></i> Statut des Candidatures</h5>', '<h5 class="mb-0 text-white"><i class="fas fa-chart-pie"></i> Statut des Candidatures</h5>')

with open('templates/dashboard/admin.html.twig', 'w', encoding='utf-8') as f:
    f.write(content)

print('Table headers fixed!')
