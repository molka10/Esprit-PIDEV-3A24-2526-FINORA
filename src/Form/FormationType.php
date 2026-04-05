<?php

namespace App\Form;

use App\Entity\Formation;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class FormationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('titre', TextType::class, [
                'label' => 'Titre'
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => false
            ])
            ->add('categorie', TextType::class, [
                'label' => 'Catégories',
                'attr' => [
                    'placeholder' => 'Ex: Bourse, ETF, Crypto'
                ],
                'help' => 'Séparez les catégories par des virgules.'
            ])
            ->add('niveau', ChoiceType::class, [
                'label' => 'Niveau',
                'placeholder' => 'Choisir un niveau',
                'choices' => [
                    'Débutant' => 'Débutant',
                    'Intermédiaire' => 'Intermédiaire',
                    'Avancé' => 'Avancé',
                ]
            ])
            ->add('is_published', ChoiceType::class, [
                'label' => 'Publié',
                'placeholder' => 'Choisir le statut',
                'choices' => [
                    'Oui' => 1,
                    'Non' => 0,
                ]
            ])
            ->add('image_url', TextType::class, [
                'label' => 'Image URL',
                'required' => false
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Formation::class,
        ]);
    }
}