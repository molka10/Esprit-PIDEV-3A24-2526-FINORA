<?php

namespace App\Form;

use App\Entity\User;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\TelType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\RepeatedType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class ProfileType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('email', EmailType::class)

            ->add('firstname', TextType::class)

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

            // 🔐 CURRENT PASSWORD (ONLY ONCE ✅)
            ->add('currentPassword', PasswordType::class, [
                'mapped' => false,
                'required' => false,
                'label' => 'Current Password'
            ])

            // 🔐 NEW PASSWORD + CONFIRM
            ->add('newPassword', RepeatedType::class, [
                'type' => PasswordType::class,
                'mapped' => false,
                'required' => false,

                'invalid_message' => 'Passwords do not match.',

                'first_options'  => [
                    'label' => 'New Password'
                ],
                'second_options' => [
                    'label' => 'Confirm Password'
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => User::class,
        ]);
    }
}