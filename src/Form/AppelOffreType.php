<?php

namespace App\Form;

use App\Entity\AppelOffre;
use App\Entity\Categorie;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\MoneyType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\GreaterThan;
use Symfony\Component\Validator\Constraints\GreaterThanOrEqual;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\Positive;
use Symfony\Component\Form\Extension\Core\Type\NumberType;

class AppelOffreType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre', TextType::class, [
                'label' => 'Titre *',
                'attr' => ['class' => 'form-control', 'placeholder' => 'Titre de l\'appel d\'offre'],
                'constraints' => [
                    new NotBlank(['message' => 'Le titre est obligatoire']),
                    new Length([
                        'min' => 3,
                        'max' => 255,
                        'minMessage' => 'Le titre doit contenir au moins {{ limit }} caractères',
                        'maxMessage' => 'Le titre ne peut pas dépasser {{ limit }} caractères',
                    ]),
                ],
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description *',
                'attr' => ['class' => 'form-control', 'rows' => 4, 'placeholder' => 'Description détaillée...'],
                'constraints' => [
                    new NotBlank(['message' => 'La description est obligatoire']),
                    new Length([
                        'min' => 10,
                        'minMessage' => 'La description doit contenir au moins {{ limit }} caractères',
                    ]),
                ],
            ])
            ->add('type', ChoiceType::class, [
    'label' => 'Type *',
    'choices' => [
        'Achat' => 'achat',
        'Partenariat' => 'partenariat',
        'Don' => 'don',
    ],
    'placeholder' => '-- Choisir un type --',
    'attr' => ['class' => 'form-control'],
])

->add('budgetMin', NumberType::class, [
    'label' => 'Budget Minimum *',
    'required' => true,
    'attr' => ['class' => 'form-control', 'placeholder' => '0.00'],
    'constraints' => [
        new NotBlank(['message' => 'Le budget minimum est obligatoire']),
        new Positive(['message' => 'Le budget minimum doit être positif']),
    ],
])
->add('budgetMax', NumberType::class, [
    'label' => 'Budget Maximum *',
    'required' => true,
    'attr' => ['class' => 'form-control', 'placeholder' => '0.00'],
    'constraints' => [
        new NotBlank(['message' => 'Le budget maximum est obligatoire']),
        new Positive(['message' => 'Le budget maximum doit être positif']),
    ],
])
            ->add('devise', ChoiceType::class, [
                'label' => 'Devise *',
                'required' => true,
                'choices' => [
                    'TND' => 'TND',
                    'EUR' => 'EUR',
                    'USD' => 'USD',
                ],
                'placeholder' => '-- Choisir une devise --',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new NotBlank(['message' => 'La devise est obligatoire']),
                ],
            ])
            ->add('dateLimite', DateType::class, [
                'label' => 'Date Limite *',
                'required' => true,
                'widget' => 'single_text',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new NotBlank(['message' => 'La date limite est obligatoire']),
                    new GreaterThan([
                        'value' => 'today',
                        'message' => 'La date limite doit être dans le futur',
                    ]),
                ],
            ])
            ->add('statut', ChoiceType::class, [
                'label' => 'Statut *',
                'choices' => [
                    'Brouillon' => 'draft',
                    'Publié' => 'published',
                    'Clôturé' => 'closed',
                ],
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new NotBlank(['message' => 'Le statut est obligatoire']),
                ],
            ])
            ->add('categorie', EntityType::class, [
                'label' => 'Catégorie *',
                'class' => Categorie::class,
                'choice_label' => 'nom',
                'required' => true,
                'placeholder' => '-- Choisir une catégorie --',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new NotBlank(['message' => 'La catégorie est obligatoire']),
                ],
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => AppelOffre::class,
        ]);
    }
}