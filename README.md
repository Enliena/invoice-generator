# Générateur de factures PDF

Application full-stack pour créer et télécharger des factures PDF épurées.

- **Backend** : Java 21 · Spring Boot 3 · iText 7
- **Frontend** : Angular 17 (composants standalone) · Reactive Forms · CSS pur

L'interface suit un design clair inspiré des Apple HIG. Aucun framework UI, aucune bibliothèque d'animation.

---

## Structure du projet

```
.
??? backend/        API Spring Boot (génération du PDF)
??? frontend/       Éditeur Angular deux panneaux + aperçu en direct
```

---

## Prérequis

- JDK 21+
- Maven 3.9+
- Node.js 18+ et npm 9+

---

## Backend ? lancement en local

```bash
cd backend
mvn spring-boot:run
```

L'API démarre sur `http://localhost:8080`.
Le CORS est activé pour `http://localhost:4200`.

### Endpoint

`POST /api/invoices/generate`

- **Corps** : JSON de la facture (voir schéma ci-dessous)
- **Réponse** : flux binaire `application/pdf`
- **En-tête** : `Content-Disposition: attachment; filename="invoice-{number}.pdf"`

### Schéma de la requête

```json
{
  "invoiceNumber": "INV-2024-001",
  "issueDate": "2024-01-15",
  "dueDate": "2024-02-15",
  "currency": "EUR",
  "sender": {
    "name": "Acme Studio",
    "address": "12 rue de Rivoli",
    "city": "Paris",
    "country": "France",
    "email": "hello@acme.studio",
    "phone": "+33 1 23 45 67 89",
    "siret": "12345678900012"
  },
  "recipient": {
    "name": "Globex Corp",
    "address": "742 Evergreen Terrace",
    "city": "Springfield",
    "country": "USA",
    "email": "ap@globex.com"
  },
  "items": [
    {
      "description": "Conseil UX",
      "quantity": 10,
      "unitPrice": 120.0,
      "vatRate": 20.0
    },
    {
      "description": "Design visuel",
      "quantity": 4,
      "unitPrice": 200.0,
      "vatRate": 20.0
    }
  ],
  "notes": "Merci de votre confiance.",
  "paymentInfo": "IBAN FR76 0000 0000 0000 0000 0000 000  ·  BIC ABCDEFGH"
}
```

### Exemple curl

```bash
curl -X POST http://localhost:8080/api/invoices/generate \
  -H "Content-Type: application/json" \
  --data-binary @sample.json \
  --output invoice.pdf
```

Enregistre d'abord le JSON ci-dessus dans `sample.json`.

---

## Frontend ? lancement en local

```bash
cd frontend
npm install
npm start
```

Ouvre `http://localhost:4200`.

Le formulaire de gauche permet d'éditer la facture ; le panneau de droite affiche un mini-récapitulatif en direct ainsi que le bouton **Télécharger le PDF**. Un clic appelle le backend et déclenche le téléchargement.

### Build pour la production

```bash
cd frontend
npm run build
```

Les fichiers statiques sont générés dans `frontend/dist/invoice-gen-frontend/`.

---

## Fonctionnalités

- Reactive Forms avec validateurs (`required`, `min`, `email`, `pattern`)
- Lignes d'articles dynamiques (ajout / suppression)
- Totaux en direct (sous-total HT, TVA, total TTC) formatés avec `Intl.NumberFormat` (`fr-FR`, devise dynamique)
- Sections de formulaire repliables avec transitions CSS fluides
- Responsive mobile : sous 768 px, la mise en page passe en colonne (formulaire puis aperçu)
- PDF A4 généré côté serveur avec iText 7, jetons de design alignés sur l'interface
