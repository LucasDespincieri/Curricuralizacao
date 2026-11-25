import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Área de Administração: só o Pastor (papel ADM no backend, "PASTOR" no front).
 * Visitante vai para o login; membro comum volta para o Início.
 * (O backend também confere o papel em cada rota /api/relatorios e /api/cidades.)
 */
export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const user = auth.currentUser();
  if (!user) return router.createUrlTree(['/login'], { queryParams: { redirect: state.url } });
  if (user.role !== 'PASTOR') return router.createUrlTree(['/inicio']);
  return true;
};
