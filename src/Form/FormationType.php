<?php

namespace App\Form;

use App\Entity\Formation;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\CallbackTransformer;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class FormationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $categoryChoices = [
            'Bourse' => 'Bourse',
            'Investissement' => 'Investissement',
            'Trading' => 'Trading',
            'Actions' => 'Actions',
            'ETF' => 'ETF',
            'Obligations' => 'Obligations',
            'Crypto' => 'Crypto',
            'Forex' => 'Forex',
            'Analyse technique' => 'Analyse technique',
            'Analyse fondamentale' => 'Analyse fondamentale',
            'Gestion des risques' => 'Gestion des risques',
            'Portefeuille' => 'Portefeuille',
            'Dividendes' => 'Dividendes',
            'Marchés financiers' => 'Marchés financiers',
            'Psychologie du trader' => 'Psychologie du trader',
            'Économie' => 'Économie',
            'Inflation' => 'Inflation',
            'Taux d’intérêt' => 'Taux d’intérêt',
            'Fiscalité' => 'Fiscalité',
        ];

        $builder
            ->add('titre', TextType::class, [
                'label' => 'Titre'
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => false
            ])
            ->add('categorie', ChoiceType::class, [
                'label' => 'Catégories',
                'choices' => $categoryChoices,
                'multiple' => true,
                'expanded' => false,
                'help' => 'Vous pouvez choisir plusieurs catégories.'
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

        $builder->get('categorie')->addModelTransformer(new CallbackTransformer(
            function ($categoriesAsString) {
                if (!$categoriesAsString) {
                    return [];
                }

                return array_map('trim', explode(',', $categoriesAsString));
            },
            function ($categoriesAsArray) {
                if (!$categoriesAsArray || !is_array($categoriesAsArray)) {
                    return '';
                }

                return implode(', ', $categoriesAsArray);
            }
        ));
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Formation::class,
        ]);
    }
}