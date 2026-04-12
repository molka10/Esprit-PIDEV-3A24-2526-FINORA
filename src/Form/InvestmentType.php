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

class InvestmentType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder

            // 🔹 NAME
            ->add('name', TextType::class, [
                'label' => 'Nom',
                'required' => true,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Ex: Projet Immobilier',
                    'minlength' => 3,
                    'maxlength' => 255
                ]
            ])

            // 🔹 CATEGORY (ENUM + fallback propre)
            ->add('category', ChoiceType::class, [
                'label' => 'Type d’investissement',
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
                    'placeholder' => 'Ex: Tunis',
                    'maxlength' => 255
                ]
            ])

            // 🔹 VALUE
            ->add('estimatedValue', NumberType::class, [
                'label' => 'Valeur estimée',
                'required' => true,
                'scale' => 2,
                'html5' => true,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Ex: 150000',
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

            // 🔥 IMAGE UPLOAD (IMPORTANT)
            ->add('imageFile', FileType::class, [
                'label' => 'Image',
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
                'label' => 'Description',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'rows' => 4,
                    'placeholder' => 'Description...',
                    'maxlength' => 2000
                ]
            ])

            // 🔹 STATUS
            ->add('status', ChoiceType::class, [
                'label' => 'Statut',
                'choices' => [
                    'Actif' => 'ACTIVE',
                    'Inactif' => 'INACTIVE',
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