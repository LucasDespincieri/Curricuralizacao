import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideArrowLeft, LucideArrowRight } from '@lucide/angular';
import { AuthService } from '../../services/auth.service';
import { IconeBibliaComponent } from '../../components/marca/icone-biblia.component';

@Component({
  selector: 'app-auth-page',
  standalone: true,
  imports: [FormsModule, RouterLink, LucideArrowRight, LucideArrowLeft, IconeBibliaComponent],
  template: `
    <div class="grid min-h-screen lg:grid-cols-2">
      <!-- Lado Esquerdo: Boas-vindas -->
      <section
        class="tema-claro relative hidden flex-col justify-between bg-slate-900 p-10 text-white lg:flex"
      >
        <div class="flex items-center gap-3">
          <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-white/10">
            <app-icone-biblia [size]="24" />
          </div>
          <p class="font-marca text-[2.6rem] leading-none font-black tracking-tight">BASE!</p>
        </div>

        <div class="max-w-lg">
          <p class="text-xs font-semibold tracking-widest text-slate-300 uppercase">
            — Um lugar para pertencer
          </p>
          <h1 class="mt-4 text-4xl font-bold leading-tight tracking-tight">
            Conectando a comunidade através da comunhão e da palavra.
          </h1>
          <p class="mt-4 text-sm leading-relaxed text-slate-300">
            Compartilhe aprendizados, encontre acolhimento e caminhe junto com pessoas que também
            buscam viver uma fé prática.
          </p>
        </div>

        <p class="text-xs text-slate-400">Uma comunidade para estudar, conversar e crescer juntos.</p>
      </section>

      <!-- Lado Direito: Formulário -->
      <section class="flex items-center justify-center bg-papel px-6 py-12">
        <div class="w-full max-w-md">
          <a
            routerLink="/inicio"
            class="mb-6 inline-flex items-center gap-1.5 text-xs font-medium text-slate-500 transition hover:text-slate-900"
          >
            <svg lucideArrowLeft [size]="14"></svg>
            <span>Voltar para o início</span>
          </a>
          <p class="text-[11px] font-semibold tracking-widest text-slate-400 uppercase">
            {{ mode() === 'LOGIN' ? 'Bem-vindo de volta' : 'Faça parte' }}
          </p>
          <h2 class="mt-1 text-3xl font-bold tracking-tight text-slate-900">
            {{ mode() === 'LOGIN' ? 'Entre na comunidade' : 'Crie sua conta' }}
          </h2>
          <p class="mt-1 text-sm text-slate-500">
            {{
              mode() === 'LOGIN'
                ? 'Continue sua jornada de comunhão e aprendizado.'
                : 'Preencha seus dados para participar dos estudos e do feed.'
            }}
          </p>

          <!-- Seletor Entrar / Cadastrar-se -->
          <div class="mt-6 grid grid-cols-2 rounded-xl border border-slate-200 bg-slate-100 p-1">
            <button
              type="button"
              (click)="switchMode('LOGIN')"
              class="rounded-lg py-2 text-xs font-semibold transition"
              [class.bg-white]="mode() === 'LOGIN'"
              [class.text-slate-900]="mode() === 'LOGIN'"
              [class.shadow-xs]="mode() === 'LOGIN'"
              [class.text-slate-500]="mode() !== 'LOGIN'"
            >
              Entrar
            </button>
            <button
              type="button"
              (click)="switchMode('REGISTER')"
              class="rounded-lg py-2 text-xs font-semibold transition"
              [class.bg-white]="mode() === 'REGISTER'"
              [class.text-slate-900]="mode() === 'REGISTER'"
              [class.shadow-xs]="mode() === 'REGISTER'"
              [class.text-slate-500]="mode() !== 'REGISTER'"
            >
              Cadastrar-se
            </button>
          </div>

          @if (errorMessage()) {
            <div
              class="mt-4 rounded-xl border border-rose-200 bg-rose-50 px-3.5 py-2.5 text-xs font-medium text-rose-700"
            >
              {{ errorMessage() }}
            </div>
          }

          @if (mode() === 'LOGIN') {
            <form (ngSubmit)="submitLogin()" class="mt-6 space-y-4">
              <div>
                <label class="mb-1.5 block text-xs font-semibold text-slate-700">E-mail</label>
                <input
                  type="email"
                  name="email"
                  [(ngModel)]="loginEmail"
                  required
                  placeholder="voce@email.com"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 focus:border-slate-400 focus:outline-none"
                />
              </div>

              <div>
                <label class="mb-1.5 block text-xs font-semibold text-slate-700">Senha</label>
                <input
                  type="password"
                  name="senha"
                  [(ngModel)]="loginSenha"
                  required
                  minlength="6"
                  placeholder="Mínimo de 6 caracteres"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2.5 text-sm text-slate-900 focus:border-slate-400 focus:outline-none"
                />
              </div>

              <button
                type="submit"
                class="mt-2 flex w-full items-center justify-center gap-2 rounded-xl bg-slate-800 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-900"
              >
                <span>Entrar na comunidade</span>
                <svg lucideArrowRight [size]="16"></svg>
              </button>
            </form>
          } @else {
            <form (ngSubmit)="submitRegister()" class="mt-6 space-y-3.5">
              <div>
                <label class="mb-1 block text-xs font-semibold text-slate-700">Nome completo</label>
                <input
                  type="text"
                  name="nome"
                  [(ngModel)]="regNome"
                  required
                  placeholder="Seu nome"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm focus:border-slate-400 focus:outline-none"
                />
              </div>

              <div>
                <label class="mb-1 block text-xs font-semibold text-slate-700">E-mail</label>
                <input
                  type="email"
                  name="regEmail"
                  [(ngModel)]="regEmail"
                  required
                  placeholder="voce@email.com"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm focus:border-slate-400 focus:outline-none"
                />
              </div>

              <div>
                <label class="mb-1 block text-xs font-semibold text-slate-700">Senha</label>
                <input
                  type="password"
                  name="regSenha"
                  [(ngModel)]="regSenha"
                  required
                  minlength="6"
                  placeholder="Mínimo de 6 caracteres"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm focus:border-slate-400 focus:outline-none"
                />
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="mb-1 block text-xs font-semibold text-slate-700">Nascimento</label>
                  <input
                    type="date"
                    name="nascimento"
                    [(ngModel)]="regNascimento"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-slate-400 focus:outline-none"
                  />
                </div>
                <div>
                  <label class="mb-1 block text-xs font-semibold text-slate-700">Bairro</label>
                  <input
                    type="text"
                    name="bairro"
                    [(ngModel)]="regBairro"
                    placeholder="Vila Mariana"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-slate-400 focus:outline-none"
                  />
                </div>
              </div>

              <div class="grid grid-cols-3 gap-3">
                <div class="col-span-2">
                  <label class="mb-1 block text-xs font-semibold text-slate-700">Cidade</label>
                  <input
                    type="text"
                    name="cidade"
                    [(ngModel)]="regCidade"
                    placeholder="São Paulo"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm focus:border-slate-400 focus:outline-none"
                  />
                </div>
                <div>
                  <label class="mb-1 block text-xs font-semibold text-slate-700">UF</label>
                  <input
                    type="text"
                    name="uf"
                    maxlength="2"
                    [(ngModel)]="regUf"
                    placeholder="SP"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm uppercase focus:border-slate-400 focus:outline-none"
                  />
                </div>
              </div>

              <button
                type="submit"
                class="mt-2 flex w-full items-center justify-center gap-2 rounded-xl bg-slate-800 px-4 py-3 text-sm font-semibold text-white transition hover:bg-slate-900"
              >
                <span>Criar conta e entrar</span>
                <svg lucideArrowRight [size]="16"></svg>
              </button>
            </form>
          }
        </div>
      </section>
    </div>
  `,
})
export class AuthPageComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  // /login?modo=cadastro abre direto na aba de cadastro
  readonly mode = signal<'LOGIN' | 'REGISTER'>(
    this.route.snapshot.queryParamMap.get('modo') === 'cadastro' ? 'REGISTER' : 'LOGIN',
  );
  readonly errorMessage = signal<string | null>(null);
  readonly submitting = signal(false);

  loginEmail = '';
  loginSenha = '';

  regNome = '';
  regEmail = '';
  regSenha = '';
  regNascimento = '';
  regBairro = '';
  regCidade = '';
  regUf = '';

  /**
   * Para onde ir depois de entrar: a página que a pessoa tentava abrir (/login?redirect=/estante)
   * ou o início. Só aceita caminhos internos, para não virar um redirecionamento para outro site.
   */
  private destinoAposLogin(): string {
    const redirect = this.route.snapshot.queryParamMap.get('redirect');
    if (redirect && redirect.startsWith('/') && !redirect.startsWith('//') && !redirect.startsWith('/login')) {
      return redirect;
    }
    return '/inicio';
  }

  switchMode(next: 'LOGIN' | 'REGISTER'): void {
    this.mode.set(next);
    this.errorMessage.set(null);
  }

  submitLogin(): void {
    if (this.submitting()) return;
    this.errorMessage.set(null);
    this.submitting.set(true);

    this.auth.login(this.loginEmail, this.loginSenha).subscribe((res) => {
      this.submitting.set(false);
      if (res.ok) {
        this.router.navigateByUrl(this.destinoAposLogin());
      } else {
        this.errorMessage.set(res.error ?? 'Não foi possível entrar.');
      }
    });
  }

  submitRegister(): void {
    if (this.submitting()) return;
    if (!this.regNome.trim() || !this.regEmail.trim() || this.regSenha.length < 6) {
      this.errorMessage.set('Preencha nome, e-mail e uma senha com pelo menos 6 caracteres.');
      return;
    }

    this.errorMessage.set(null);
    this.submitting.set(true);

    this.auth
      .register({
        nome: this.regNome,
        email: this.regEmail,
        senha: this.regSenha,
        nascimento: this.regNascimento,
        bairro: this.regBairro,
        cidade: this.regCidade,
        uf: this.regUf,
      })
      .subscribe((res) => {
        this.submitting.set(false);
        if (res.ok) {
          this.router.navigateByUrl(this.destinoAposLogin());
        } else {
          this.errorMessage.set(res.error ?? 'Não foi possível criar a conta.');
        }
      });
  }
}
