<?php

namespace App\Form;

use App\Entity\Investment;
use App\Enum\InvestmentCategory;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

// TYPES
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;

// VALIDATION
use Symfony\Component\Validator\Constraints\File;

use Symfony\Component\Form\Extension\Core\Type\IntegerType;

class InvestmentType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder

            // 🔹 NAME
            ->add('name', TextType::class, [
                'label' => 'Nom du projet',
                'required' => true,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Ex: Résidence Les Jardins de Tunis',
                    'minlength' => 3,
                    'maxlength' => 255
                ]
            ])

            // 🔹 CATEGORY
            ->add('category', ChoiceType::class, [
                'label' => 'Type de bien',
                'choices' => InvestmentCategory::formChoices(),
                'placeholder' => 'Choisir une catégorie',
                'required' => true,
                'attr' => ['class' => 'form-select']
            ])

            // 🔹 LOCATION
            ->add('location', TextType::class, [
                'label' => 'Localisation',
                'required' => true,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Ex: Tunis, La Marsa',
                    'maxlength' => 255
                ]
            ])

            // 🔹 VALUE
            ->add('estimatedValue', NumberType::class, [
                'label' => 'Valeur totale du bien (TND)',
                'required' => true,
                'scale' => 2,
                'html5' => true,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Ex: 500000',
                    'min' => 0,
                    'step' => '0.01'
                ]
            ])

            // 🔹 RISK
            ->add('riskLevel', ChoiceType::class, [
                'label' => 'Niveau de risque',
                'choices' => [
                    'Faible' => 'LOW',
                    'Moyen' => 'MEDIUM',
                    'Élevé' => 'HIGH',
                ],
                'placeholder' => 'Choisir un niveau',
                'required' => true,
                'attr' => ['class' => 'form-select']
            ])

            // 🔥 IMAGE UPLOAD
            ->add('imageFile', FileType::class, [
                'label' => 'Photo du projet',
                'mapped' => false,
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'accept' => 'image/*'
                ],
                'constraints' => [
                    new File([
                        'maxSize' => '2M',
                        'mimeTypes' => ['image/jpeg', 'image/png'],
                        'mimeTypesMessage' => 'Formats acceptés : JPG, PNG',
                    ])
                ]
            ])

            // 🔹 DESCRIPTION
            ->add('description', TextareaType::class, [
                'label' => 'Description du projet',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'rows' => 5,
                    'placeholder' => 'Décrivez le projet d\'investissement, son potentiel, son emplacement...',
                    'maxlength' => 2000
                ]
            ])

            // 🔹 STATUS
            ->add('status', ChoiceType::class, [
                'label' => 'Statut',
                'choices' => [
                    'En cours de collecte' => 'ACTIVE',
                    'Collecte terminée' => 'INACTIVE',
                ],
                'placeholder' => 'Choisir un statut',
                'required' => true,
                'attr' => ['class' => 'form-select']
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Investment::class,
            'attr' => [
                'class' => 'investment-form',
                'novalidate' => 'novalidate',
            ],
        ]);
    }
}