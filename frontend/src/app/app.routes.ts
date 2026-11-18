import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { FeedPageComponent } from './pages/feed/feed-page.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', redirectTo: 'inicio', pathMatch: 'full' },
      { path: 'inicio', component: FeedPageComponent },
      { path: '**', redirectTo: 'inicio' },
    ],
  },
];
