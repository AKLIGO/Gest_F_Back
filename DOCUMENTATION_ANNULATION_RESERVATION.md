# Documentation - Système d'Annulation de Réservation

## Vue d'ensemble

Le système d'annulation permet aux clients d'annuler leurs réservations dans les **24 heures** suivant leur création. Après ce délai, l'annulation n'est plus possible.

## Endpoints API disponibles

### 1. Vérifier si une annulation est possible (Simple)
**Endpoint:** `GET /api/reservations/{id}/can-cancel`

**Description:** Retourne un booléen indiquant si la réservation peut être annulée.

**Exemple de réponse:**
```json
true
```

---

### 2. Obtenir les informations détaillées sur l'annulation
**Endpoint:** `GET /api/reservations/{id}/cancellation-info`

**Description:** Retourne des informations détaillées sur la possibilité d'annulation incluant le nombre d'heures restantes et un message explicatif.

**Exemple de réponse (annulation possible):**
```json
{
  "canCancel": true,
  "message": "Vous pouvez annuler cette réservation",
  "hoursRemaining": 18,
  "reservationId": 123
}
```

**Exemple de réponse (délai dépassé):**
```json
{
  "canCancel": false,
  "message": "Le délai d'annulation de 24h est dépassé",
  "hoursRemaining": 0,
  "reservationId": 123
}
```

---

### 3. Annuler une réservation
**Endpoint:** `POST /api/reservations/{id}/cancel`

**Description:** Annule la réservation si elle est éligible (créée il y a moins de 24h).

**Exemple de réponse (succès):**
```json
{
  "id": 123,
  "dateDebut": "2025-02-01",
  "dateFin": "2025-02-05",
  "montant": 500.0,
  "appartementNom": "Studio Centre Ville",
  "appartementAdresse": "123 Rue de la Paix, Paris",
  "utilisateurNom": "Dupont",
  "utilisateurPrenoms": "Jean",
  "statut": "ANNULEE"
}
```

**Exemple de réponse (erreur - délai dépassé):**
```json
"Le délai d'annulation de 24h est dépassé. Vous ne pouvez plus annuler cette réservation."
```

---

## Règles métier

1. **Délai d'annulation:** Une réservation peut être annulée dans les 24 heures suivant sa création (`createdAt`).

2. **Statuts éligibles:** Seules les réservations avec les statuts suivants peuvent être annulées :
   - `EN_ATTENTE`
   - `VALIDEE`
   - `CONFIRMEE`
   - `PARTIELLEMENT_PAYEE`
   - `PAYEE`

3. **Statuts non éligibles:**
   - `ANNULEE` (déjà annulée)
   - `TERMINER` (réservation terminée)

4. **Autorisation:** Seul le propriétaire de la réservation peut l'annuler.

5. **Notification:** Un email de confirmation est envoyé automatiquement après l'annulation.

---

## Exemples d'utilisation côté client (Angular/TypeScript)

### Service Angular

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CancellationInfo {
  canCancel: boolean;
  message: string;
  hoursRemaining: number;
  reservationId: number;
}

export interface ReservationResponse {
  id: number;
  dateDebut: string;
  dateFin: string;
  montant: number;
  appartementNom: string;
  appartementAdresse: string;
  utilisateurNom: string;
  utilisateurPrenoms: string;
  statut: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = 'http://localhost:8082/api/reservations';

  constructor(private http: HttpClient) {}

  // Vérifier si l'annulation est possible (simple)
  canCancelReservation(reservationId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${reservationId}/can-cancel`);
  }

  // Obtenir les informations détaillées
  getCancellationInfo(reservationId: number): Observable<CancellationInfo> {
    return this.http.get<CancellationInfo>(`${this.apiUrl}/${reservationId}/cancellation-info`);
  }

  // Annuler une réservation
  cancelReservation(reservationId: number): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(`${this.apiUrl}/${reservationId}/cancel`, {});
  }
}
```

---

### Composant Angular

```typescript
import { Component, OnInit } from '@angular/core';
import { ReservationService, CancellationInfo } from './services/reservation.service';

@Component({
  selector: 'app-mes-reservations',
  templateUrl: './mes-reservations.component.html'
})
export class MesReservationsComponent implements OnInit {
  reservations: any[] = [];
  cancellationInfos: Map<number, CancellationInfo> = new Map();

  constructor(private reservationService: ReservationService) {}

  ngOnInit() {
    this.loadReservations();
  }

  loadReservations() {
    // Charger vos réservations ici
    // Ensuite, pour chaque réservation, charger les infos d'annulation
    this.reservations.forEach(reservation => {
      this.loadCancellationInfo(reservation.id);
    });
  }

  loadCancellationInfo(reservationId: number) {
    this.reservationService.getCancellationInfo(reservationId).subscribe({
      next: (info) => {
        this.cancellationInfos.set(reservationId, info);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des infos d\'annulation', err);
      }
    });
  }

  cancelReservation(reservationId: number) {
    if (confirm('Êtes-vous sûr de vouloir annuler cette réservation ?')) {
      this.reservationService.cancelReservation(reservationId).subscribe({
        next: (response) => {
          alert('Réservation annulée avec succès !');
          this.loadReservations(); // Recharger la liste
        },
        error: (err) => {
          alert(err.error || 'Erreur lors de l\'annulation');
        }
      });
    }
  }

  getCancellationInfo(reservationId: number): CancellationInfo | null {
    return this.cancellationInfos.get(reservationId) || null;
  }

  canCancel(reservationId: number): boolean {
    const info = this.getCancellationInfo(reservationId);
    return info?.canCancel || false;
  }
}
```

---

### Template HTML

```html
<div class="reservations-container">
  <h2>Mes Réservations</h2>
  
  <div *ngFor="let reservation of reservations" class="reservation-card">
    <h3>{{ reservation.appartementNom }}</h3>
    <p>Du {{ reservation.dateDebut | date:'dd/MM/yyyy' }} au {{ reservation.dateFin | date:'dd/MM/yyyy' }}</p>
    <p>Montant: {{ reservation.montant | currency:'EUR' }}</p>
    <p>Statut: <span [class]="'statut-' + reservation.statut.toLowerCase()">{{ reservation.statut }}</span></p>
    
    <!-- Afficher les informations d'annulation -->
    <div *ngIf="getCancellationInfo(reservation.id) as info" class="cancellation-info">
      <div *ngIf="info.canCancel" class="alert alert-info">
        <i class="fas fa-info-circle"></i>
        {{ info.message }}
        <br>
        <small>Temps restant pour annuler: {{ info.hoursRemaining }} heure(s)</small>
        <br>
        <button (click)="cancelReservation(reservation.id)" class="btn btn-danger mt-2">
          <i class="fas fa-times"></i> Annuler la réservation
        </button>
      </div>
      
      <div *ngIf="!info.canCancel && reservation.statut !== 'ANNULEE'" class="alert alert-warning">
        <i class="fas fa-exclamation-triangle"></i>
        {{ info.message }}
      </div>
    </div>
  </div>
</div>
```

---

### CSS (optionnel)

```css
.reservation-card {
  border: 1px solid #ddd;
  padding: 20px;
  margin: 10px 0;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.cancellation-info {
  margin-top: 15px;
}

.alert {
  padding: 12px;
  border-radius: 4px;
  margin-top: 10px;
}

.alert-info {
  background-color: #d1ecf1;
  color: #0c5460;
  border: 1px solid #bee5eb;
}

.alert-warning {
  background-color: #fff3cd;
  color: #856404;
  border: 1px solid #ffeeba;
}

.statut-annulee {
  color: #dc3545;
  font-weight: bold;
}

.statut-confirmee {
  color: #28a745;
  font-weight: bold;
}

.statut-en_attente {
  color: #ffc107;
  font-weight: bold;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-danger:hover {
  background-color: #c82333;
}
```

---

## Workflow complet

1. **Client crée une réservation** → Statut: `EN_ATTENTE` ou `CONFIRMEE`
2. **Client consulte ses réservations** → Appel GET `/api/reservations/mes-reservations`
3. **Pour chaque réservation** → Appel GET `/api/reservations/{id}/cancellation-info`
4. **Affichage du bouton d'annulation** si `canCancel = true`
5. **Client clique sur annuler** → Appel POST `/api/reservations/{id}/cancel`
6. **Confirmation** → Email envoyé + Statut devient `ANNULEE`

---

## Gestion des erreurs

| Code | Message | Signification |
|------|---------|---------------|
| 400 | "Le délai d'annulation de 24h est dépassé..." | Le délai est dépassé |
| 400 | "Cette réservation est déjà annulée" | La réservation a déjà été annulée |
| 400 | "Cette réservation est terminée..." | La réservation est dans le passé |
| 400 | "Vous n'êtes pas autorisé..." | L'utilisateur n'est pas le propriétaire |
| 404 | "Réservation non trouvée" | ID invalide |

---

## Notes importantes

- Le calcul des 24h est basé sur le champ `createdAt` de la réservation
- L'annulation change uniquement le statut à `ANNULEE`, elle ne supprime pas la réservation
- Un email de notification est automatiquement envoyé lors de l'annulation
- Seul l'utilisateur propriétaire de la réservation peut l'annuler
- La vérification de sécurité est faite via le token JWT de l'utilisateur connecté

---

## Tests recommandés

1. ✅ Tester l'annulation dans les 24h
2. ✅ Tester l'annulation après 24h (doit échouer)
3. ✅ Tester l'annulation d'une réservation déjà annulée (doit échouer)
4. ✅ Tester l'annulation d'une réservation d'un autre utilisateur (doit échouer)
5. ✅ Vérifier l'envoi de l'email de confirmation
