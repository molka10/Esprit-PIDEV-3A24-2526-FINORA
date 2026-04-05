<?php

namespace App\Form;

use App\Entity\Investment;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

// TYPES
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\UrlType;
use Symfony\Component\Form\Extension\Core\Type\TextType;

class InvestmentType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder

            // NAME
            ->add('name', TextType::class, [
                'attr' => [
                    'placeholder' => 'Land Development Project'
                ]
            ])

            // CATEGORY
            ->add('category', TextType::class, [
                'attr' => [
                    'placeholder' => 'Real Estate'
                ]
            ])

            // LOCATION
            ->add('location', TextType::class, [
                'attr' => [
                    'placeholder' => 'Tunis, Tunisia'
                ]
            ])

            // VALUE
            ->add('estimatedValue', NumberType::class, [
                'scale' => 2,
                'attr' => [
                    'placeholder' => '150000'
                ]
            ])

            // RISK
            ->add('riskLevel', ChoiceType::class, [
                'choices' => [
                    'Low' => 'LOW',
                    'Medium' => 'MEDIUM',
                    'High' => 'HIGH',
                ],
                'placeholder' => 'Select risk level'
            ])

            // IMAGE
            ->add('imageUrl', UrlType::class, [
                'required' => false,
                'attr' => [
                    'placeholder' => 'https://example.com/image.jpg'
                ]
            ])

            // DESCRIPTION
            ->add('description', TextareaType::class, [
                'required' => false,
                'attr' => [
                    'rows' => 4,
                    'placeholder' => 'Write a short description...'
                ]
            ])

            // STATUS
            ->add('status', ChoiceType::class, [
                'choices' => [
                    'Active' => 'ACTIVE',
                    'Inactive' => 'INACTIVE',
                ],
                'placeholder' => 'Select status'
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Investment::class,
        ]);
    }
}