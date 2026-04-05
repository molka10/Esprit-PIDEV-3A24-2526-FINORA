<?php

namespace App\Form;

use App\Entity\User;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\CheckboxType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\TelType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class UserType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('email', EmailType::class)

            ->add('firstname', TextType::class, [
                'label' => 'Username'
            ])

            ->add('phone', TelType::class, [
                'required' => false
            ])

            ->add('address', TextType::class, [
                'required' => false
            ])

            ->add('dateOfBirth', DateType::class, [
                'widget' => 'single_text',
                'required' => false
            ])

            ->add('roleType', ChoiceType::class, [
                'choices' => [
                    'Investisseur' => 'investisseur',
                    'Entreprise' => 'entreprise',
                ]
            ])

            ->add('role', ChoiceType::class, [
                'choices' => [
                    'User' => 'USER',
                    'Admin' => 'ADMIN',
                ]
            ])

            ->add('isActive', CheckboxType::class, [
                'required' => false
            ])

            ->add('password', PasswordType::class, [
                'required' => false,
                'mapped' => false
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => User::class,
        ]);
    }
}