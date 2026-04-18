<?php

namespace App\Form;

use App\Entity\Action;
use App\Entity\Bourse;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class ActionType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('bourse', EntityType::class, [
                'label'        => 'Bourse',
                'class'        => Bourse::class,
                'choice_label' => 'nomBourse',
                'placeholder'  => '-- Sélectionnez une bourse --',
                'attr'         => ['class' => 'form-select'],
            ])
            ->add('symbole', TextType::class, [
                'label' => 'Symbole',
                'attr'  => [
                    'class'       => 'form-control',
                    'placeholder' => 'Ex: AAPL',
                    'maxlength'   => 20,
                    'style'       => 'text-transform: uppercase;',
                ],
            ])
            ->add('nomEntreprise', TextType::class, [
                'label' => 'Nom de l\'entreprise',
                'attr'  => [
                    'class'       => 'form-control',
                    'placeholder' => 'Ex: Apple Inc.',
                    'maxlength'   => 150,
                ],
            ])
            ->add('secteur', ChoiceType::class, [
                'label'   => 'Secteur',
                'choices' => [
                    'Technologie'      => 'Technologie',
                    'Finance'          => 'Finance',
                    'Santé'            => 'Santé',
                    'Énergie'          => 'Énergie',
                    'Industrie'        => 'Industrie',
                    'Consommation'     => 'Consommation',
                    'Télécommunications' => 'Télécommunications',
                    'Immobilier'       => 'Immobilier',
                    'Autre'            => 'Autre',
                ],
                'attr' => ['class' => 'form-select'],
            ])
            ->add('prixUnitaire', NumberType::class, [
                'label' => 'Prix unitaire',
                'scale' => 2,
                'attr'  => [
                    'class'       => 'form-control',
                    'placeholder' => '0.00',
                    'step'        => '0.01',
                    'min'         => '0.01',
                ],
            ])
            ->add('quantiteDisponible', IntegerType::class, [
                'label' => 'Quantité disponible',
                'attr'  => [
                    'class'       => 'form-control',
                    'placeholder' => '0',
                    'min'         => '0',
                ],
            ])
            ->add('statut', ChoiceType::class, [
                'label'   => 'Statut',
                'choices' => [
                    'Disponible'   => 'DISPONIBLE',
                    'Indisponible' => 'INDISPONIBLE',
                ],
                'attr' => ['class' => 'form-select'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Action::class,
        ]);
    }
}