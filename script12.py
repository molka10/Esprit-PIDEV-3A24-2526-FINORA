import re

with open('templates/admin_base.html.twig', 'r', encoding='utf-8') as f:
    content = f.read()

# Sidebar footer
new_footer = '''<!-- Sidebar footer START -->
			<div class="px-3 mt-auto pt-3">
				<div class="d-flex align-items-center justify-content-center text-primary-hover">
						<a class="h5 mb-0 text-body" href="{{ path('app_dashboard') }}" data-bs-toggle="tooltip" data-bs-placement="top" title="Home">
							<i class="bi bi-globe"></i> Retour au site
						</a>
				</div>
			</div>
			<!-- Sidebar footer END -->'''

content = re.sub(r'<!-- Sidebar footer START -->.*?<!-- Sidebar footer END -->', new_footer, content, flags=re.DOTALL)

with open('templates/admin_base.html.twig', 'w', encoding='utf-8') as f:
    f.write(content)
