import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ResultsComponent } from './results/results.component';
import { FavoritesComponent } from './favorites/favorites.component';

const routes: Routes = [
  // { path: 'home', redirectTo: '/', pathMatch: 'full' },
  { path: '', component: ResultsComponent },
  { path: 'results', component: ResultsComponent },
  { path: 'favorites', component: FavoritesComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
