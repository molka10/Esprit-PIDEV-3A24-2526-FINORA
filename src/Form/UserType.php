<?php

namespace App\Form;

use App\Entity\User;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Bundle\SecurityBundle\Security;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\Email;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\File;

class UserType extends AbstractType
{
    private Security $security;

    public function __construct(Security $security)
    {
        $this->security = $security;
    }

    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $currentUser = $this->security->getUser();

        if ($currentUser && in_array('ROLE_ADMIN', $currentUser->getRoles())) {
            $roles = [
                'User' => 'USER',
                'Entreprise' => 'ENTREPRISE',
                'Admin' => 'ADMIN',
            ];
        } else {
            $roles = [
                'User' => 'USER',
                'Entreprise' => 'ENTREPRISE',
            ];
        }

        $builder
            ->add('username', null, [
                'constraints' => [
                    new NotBlank(['message' => 'Username is required']),
                ],
            ])
            ->add('email', null, [
                'constraints' => [
                    new NotBlank(['message' => 'Email is required']),
                    new Email(['message' => 'Invalid email']),
                ],
            ])
            ->add('password', PasswordType::class, [
                'mapped' => false,
                'required' => false,
                'constraints' => [
                    new Length([
                        'min' => 6,
                        'minMessage' => 'Password must be at least 6 characters',
                    ]),
                ],
            ])
            // 🔥 IMAGE UPLOAD
            ->add('image', FileType::class, [
                'mapped' => false,
                'required' => false,
                'constraints' => [
                    new File([
                        'maxSize' => '2M',
                        'mimeTypes' => ['image/jpeg', 'image/png'],
                        'mimeTypesMessage' => 'Upload a valid image (JPG/PNG)',
                    ])
                ],
            ])
            ->add('role', ChoiceType::class, [
                'choices' => $roles,
            ])
            ->add('phone')
            ->add('address')
            ->add('date_of_birth');

        if ($currentUser && in_array('ROLE_ADMIN', $currentUser->getRoles())) {
            $builder->add('isVerified');
        }
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => User::class,
        ]);
    }
}