import { Component, input } from '@angular/core';

/** Ícone da marca: uma bíblia (livro com uma cruz na capa), no mesmo traço dos ícones Lucide. */
@Component({
  selector: 'app-icone-biblia',
  standalone: true,
  host: { class: 'inline-flex' },
  template: `
    <svg
      xmlns="http://www.w3.org/2000/svg"
      [attr.width]="size()"
      [attr.height]="size()"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
    >
      <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H19a1 1 0 0 1 1 1v18a1 1 0 0 1-1 1H6.5a1 1 0 0 1 0-5H20" />
      <path d="M12 5.5v8" />
      <path d="M9.25 8.5h5.5" />
    </svg>
  `,
})
export class IconeBibliaComponent {
  readonly size = input(20);
}
