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
                'label' => 'Titre',
                'attr' => [
                    'placeholder' => 'Entrez le titre de la formation'
                ]
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => false,
                'attr' => [
                    'placeholder' => 'Entrez une description'
                ]
            ])
            ->add('categorie', TextType::class, [
                'label' => 'Catégorie',
                'attr' => [
                    'placeholder' => 'Entrez la catégorie'
                ]
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
                'required' => false,
                'attr' => [
                    'placeholder' => 'https://example.com/image.jpg'
                ]
            ])
            ->add('created_at', TextType::class, [
                'label' => 'Date de création',
                'attr' => [
                    'placeholder' => 'YYYY-MM-DD'
                ]
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Formation::class,
        ]);
    }
}