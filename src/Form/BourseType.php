<?php

namespace App\Form;

use App\Entity\Bourse;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class BourseType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nomBourse', TextType::class, [
                'label' => 'Nom de la Bourse',
                'attr'  => [
                    'class'       => 'form-control',
                    'placeholder' => 'Ex: Bourse de Paris',
                    'maxlength'   => 100,
                ],
            ])
            ->add('pays', TextType::class, [
                'label' => 'Pays',
                'attr'  => [
                    'class'       => 'form-control',
                    'placeholder' => 'Ex: France',
                    'maxlength'   => 50,
                ],
            ])
            ->add('devise', TextType::class, [
                'label' => 'Devise',
                'attr'  => [
                    'class'       => 'form-control',
                    'placeholder' => 'Ex: USD, EUR, TND',
                    'maxlength'   => 3,
                    'style'       => 'text-transform: uppercase;',
                ],
            ])
            ->add('statut', ChoiceType::class, [
                'label'   => 'Statut',
                'attr'    => ['class' => 'form-select'],
                'choices' => [
                    'Active'   => 'ACTIVE',
                    'Inactive' => 'INACTIVE',
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Bourse::class,
        ]);
    }
}