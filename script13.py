import re

with open('templates/dashboard/admin.html.twig', 'r', encoding='utf-8') as f:
    content = f.read()

# I will replace everything between <!-- Title --> and {# GRAPHIQUES #} with the correct layout

new_content = '''<!-- Title -->
<div class="row">
    <div class="col-12 mb-3">
        <h1 class="h3 mb-2 mb-sm-0">Dashboard Admin</h1>
    </div>
</div>

<h5 class="mb-3 mt-4"><i class="fas fa-file-contract text-warning"></i> Appels d'Offre</h5>
<div class="row g-4 mb-4">
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-primary bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ totalAppels }}</h2>
                    <span class="mb-0 h6 fw-light">Total</span>
                </div>
                <div class="icon-lg rounded-circle bg-primary text-white mb-0"><i class="fas fa-file-contract fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-success bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ appelPublies }}</h2>
                    <span class="mb-0 h6 fw-light">Publiés</span>
                </div>
                <div class="icon-lg rounded-circle bg-success text-white mb-0"><i class="fas fa-check-circle fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-danger bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ appelClotures }}</h2>
                    <span class="mb-0 h6 fw-light">Clôturés</span>
                </div>
                <div class="icon-lg rounded-circle bg-danger text-white mb-0"><i class="fas fa-lock fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-secondary bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ appelBrouillons }}</h2>
                    <span class="mb-0 h6 fw-light">Brouillons</span>
                </div>
                <div class="icon-lg rounded-circle bg-secondary text-white mb-0"><i class="fas fa-edit fa-fw"></i></div>
            </div>
        </div>
    </div>
</div>

<h5 class="mb-3"><i class="fas fa-users text-warning"></i> Candidatures</h5>
<div class="row g-4 mb-4">
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-primary bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ totalCandidatures }}</h2>
                    <span class="mb-0 h6 fw-light">Total</span>
                </div>
                <div class="icon-lg rounded-circle bg-primary text-white mb-0"><i class="fas fa-users fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-success bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ candidaturesAcceptees }}</h2>
                    <span class="mb-0 h6 fw-light">Acceptées</span>
                </div>
                <div class="icon-lg rounded-circle bg-success text-white mb-0"><i class="fas fa-thumbs-up fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-danger bg-opacity-10 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ candidaturesRejetes }}</h2>
                    <span class="mb-0 h6 fw-light">Rejetées</span>
                </div>
                <div class="icon-lg rounded-circle bg-danger text-white mb-0"><i class="fas fa-thumbs-down fa-fw"></i></div>
            </div>
        </div>
    </div>
    <!-- Counter item -->
    <div class="col-md-6 col-xxl-3">
        <div class="card card-body bg-warning bg-opacity-15 p-4 h-100">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <h2 class="purecounter mb-0 fw-bold">{{ candidaturesEnAttente }}</h2>
                    <span class="mb-0 h6 fw-light">En Attente</span>
                </div>
                <div class="icon-lg rounded-circle bg-warning text-white mb-0"><i class="fas fa-hourglass-half fa-fw"></i></div>
            </div>
        </div>
    </div>
</div>

'''

content = re.sub(r'<!-- Title -->.*?{# GRAPHIQUES #}', new_content + '{# GRAPHIQUES #}', content, flags=re.DOTALL)

with open('templates/dashboard/admin.html.twig', 'w', encoding='utf-8') as f:
    f.write(content)

