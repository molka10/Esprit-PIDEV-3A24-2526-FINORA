import re

with open('templates/dashboard/admin.html.twig', 'r', encoding='utf-8') as f:
    content = f.read()

new_content = '''<!-- Title -->
<div class="row">
    <div class="col-12 mb-3">
        <h1 class="h3 mb-2 mb-sm-0">Dashboard Admin</h1>
    </div>
</div>

{# ROW 1 - Appels d'offre #}
<h5 class="mb-3 mt-4"><i class="fas fa-file-contract text-warning"></i> Appels d'Offre</h5>
<div class="row mb-4">
    <div class="col-md-3">
        <div class="card text-white mb-3" style="background:#1a1a2e;">
            <div class="card-body text-center">
                <i class="fas fa-file-contract fa-2x mb-2"></i>
                <h3 class="text-white">{{ totalAppels }}</h3>
                <p class="mb-0">Total</p>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white mb-3" style="background:#28a745;">
            <div class="card-body text-center">
                <i class="fas fa-check-circle fa-2x mb-2"></i>
                <h3 class="text-white">{{ appelPublies }}</h3>
                <p class="mb-0">Publiés</p>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white mb-3" style="background:#dc3545;">
            <div class="card-body text-center">
                <i class="fas fa-lock fa-2x mb-2"></i>
                <h3 class="text-white">{{ appelClotures }}</h3>
                <p class="mb-0">Clôturés</p>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white mb-3" style="background:#6c757d;">
            <div class="card-body text-center">
                <i class="fas fa-edit fa-2x mb-2"></i>
                <h3 class="text-white">{{ appelBrouillons }}</h3>
                <p class="mb-0">Brouillons</p>
            </div>
        </div>
    </div>
</div>

{# ROW 2 - Candidatures #}
<h5 class="mb-3 mt-4"><i class="fas fa-users text-warning"></i> Candidatures</h5>
<div class="row mb-4">
    <div class="col-md-3">
        <div class="card text-white mb-3" style="background:#1a1a2e;">
            <div class="card-body text-center">
                <i class="fas fa-users fa-2x mb-2"></i>
                <h3 class="text-white">{{ totalCandidatures }}</h3>
                <p class="mb-0">Total</p>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white mb-3" style="background:#28a745;">
            <div class="card-body text-center">
                <i class="fas fa-thumbs-up fa-2x mb-2"></i>
                <h3 class="text-white">{{ candidaturesAcceptees }}</h3>
                <p class="mb-0">Acceptées</p>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white mb-3" style="background:#dc3545;">
            <div class="card-body text-center">
                <i class="fas fa-thumbs-down fa-2x mb-2"></i>
                <h3 class="text-white">{{ candidaturesRejetes }}</h3>
                <p class="mb-0">Rejetées</p>
            </div>
        </div>
    </div>
    <div class="col-md-3">
        <div class="card text-white mb-3" style="background:#ffc107;">
            <div class="card-body text-center">
                <i class="fas fa-hourglass-half fa-2x mb-2"></i>
                <h3 class="text-white">{{ candidaturesEnAttente }}</h3>
                <p class="mb-0">En Attente</p>
            </div>
        </div>
    </div>
</div>
'''

content = re.sub(r'<!-- Title -->.*?{# GRAPHIQUES #}', new_content + '{# GRAPHIQUES #}', content, flags=re.DOTALL)

with open('templates/dashboard/admin.html.twig', 'w', encoding='utf-8') as f:
    f.write(content)
