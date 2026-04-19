import re

with open('templates/base.html.twig', 'r', encoding='utf-8') as f:
    content = f.read()

nav_content = '''<ul class="navbar-nav navbar-nav-scroll ms-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="{{ path('app_dashboard') }}">Dashboard</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="{{ path('app_appel_offre_index') }}">Appels d'Offre</a>
                    </li>
                    {% if app.session.get('role') == 'entreprise' %}
                    <li class="nav-item">
                        <a class="nav-link" href="{{ path('app_candidature_index') }}">Mes Candidatures</a>
                    </li>
                    {% endif %}
                    
                    <!-- ROLE SWITCHER -->
                    <li class="nav-item ms-3 d-flex align-items-center">
                        <form method="post" action="{{ path('app_switch_role') }}" class="m-0">
                            <select name="role" class="form-select form-select-sm" onchange="this.form.submit()">
                                <option value="visiteur" {{ app.session.get('role') == 'visiteur' or app.session.get('role') == null ? 'selected' : '' }}>?? Visiteur</option>
                                <option value="entreprise" {{ app.session.get('role') == 'entreprise' ? 'selected' : '' }}>?? Entreprise</option>
                            </select>
                        </form>
                    </li>
                </ul>'''

content = re.sub(r'<ul class="navbar-nav navbar-nav-scroll ms-auto">.*?</ul>', nav_content, content, flags=re.DOTALL)

with open('templates/base.html.twig', 'w', encoding='utf-8') as f:
    f.write(content)
