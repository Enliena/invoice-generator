import { Injectable } from '@angular/core';

const DICT: Record<string, string> = {
  'app.title': 'Nouvelle facture',
  'app.subtitle': 'Remplissez les détails avant de générer un PDF.',

  'section.sender': 'Vos informations',
  'section.recipient': 'Facturer à',
  'section.meta': 'Détails de la facture',
  'section.items': 'Lignes de facturation',
  'section.footer': 'Notes et informations de paiement',

  'field.name': 'Nom',
  'field.email': 'E-mail',
  'field.address': 'Adresse',
  'field.city': 'Ville',
  'field.country': 'Pays',
  'field.phone': 'Téléphone',
  'field.siret': 'SIRET',
  'field.number': 'Numéro',
  'field.issueDate': "Date d'émission",
  'field.dueDate': "Date d'échéance",
  'field.currency': 'Devise',
  'field.notes': 'Notes',
  'field.paymentInfo': 'Informations de paiement',

  'placeholder.companyName': 'Votre société',
  'placeholder.companyEmail': 'contact@societe.com',
  'placeholder.clientName': 'Nom du client',
  'placeholder.clientEmail': 'client@email.com',
  'placeholder.street': 'Rue',
  'placeholder.city': 'Ville',
  'placeholder.country': 'Pays',
  'placeholder.phone': '+33',
  'placeholder.siret': '14 chiffres',
  'placeholder.notes': 'Merci de votre confiance',
  'placeholder.paymentInfo': 'IBAN, BIC, conditions de paiement',
  'placeholder.itemDescription': 'Description de la prestation',

  'error.required': 'Requis',
  'error.email': 'E-mail invalide',
  'error.pattern': 'Format invalide',
  'error.min': 'Doit être supérieur ou égal à 0',
  'error.invalid': 'Invalide',
  'error.fix': 'Veuillez corriger les champs en surbrillance.',
  'error.network':
    'Impossible de joindre le serveur. Le backend est-il démarré sur :8080 ?',
  'error.server': 'Erreur serveur',

  'items.description': 'Description',
  'items.qty': 'Qté',
  'items.unitPrice': 'Prix unitaire',
  'items.vat': 'TVA %',
  'items.total': 'Total',
  'items.add': '+ Ajouter une ligne',
  'items.remove': 'Supprimer la ligne',

  'preview.invoice': 'Facture',
  'preview.from': 'De',
  'preview.billTo': 'Facturer à',
  'preview.issue': 'Émission',
  'preview.due': 'Échéance',
  'preview.itemsCount': 'Lignes',
  'preview.subtotal': 'Sous-total',
  'preview.vat': 'TVA',
  'preview.total': 'Total TTC',
  'preview.download': 'Télécharger le PDF',
  'preview.generating': 'Génération...',
};

/** Registre des libellés affichés dans l'interface. */
@Injectable({ providedIn: 'root' })
export class TranslationService {
  t(key: string): string {
    return DICT[key] ?? key;
  }

  locale(): string {
    return 'fr-FR';
  }
}
