<?php

namespace App\Service;

use Symfony\Component\HttpFoundation\File\Exception\FileException;
use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\String\Slugger\SluggerInterface;

final class InvestmentImageUploader
{
    public function __construct(
        private readonly string $targetDirectory,
        private readonly SluggerInterface $slugger,
    ) {
    }

    public function upload(UploadedFile $file): string
    {
        $original = pathinfo($file->getClientOriginalName(), \PATHINFO_FILENAME);
        $safe = $this->slugger->slug($original)->lower();
        $newFilename = $safe.'-'.uniqid('', true).'.'.$file->guessExtension();

        try {
            $file->move($this->targetDirectory, $newFilename);
        } catch (FileException $e) {
            throw new \RuntimeException('Could not save image: '.$e->getMessage(), 0, $e);
        }

        return $newFilename;
    }

    public function remove(?string $storedValue): void
    {
        if ($storedValue === null || $storedValue === '') {
            return;
        }
        if (str_contains($storedValue, '://')) {
            return;
        }
        $path = $this->targetDirectory.\DIRECTORY_SEPARATOR.basename($storedValue);
        if (is_file($path)) {
            @unlink($path);
        }
    }

    public function getTargetDirectory(): string
    {
        return $this->targetDirectory;
    }
}
