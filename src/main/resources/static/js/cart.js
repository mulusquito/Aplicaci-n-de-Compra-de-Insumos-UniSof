/**
 * Carrito de compras - UNISOF
 * Persiste en localStorage. Usado en ventas.html para el flujo de checkout con Mercado Pago.
 *
 * Cada fila del carrito tiene un `lineId` estable (UUID). `id` coincide con `lineId` para que
 * cambiar la talla en una fila no fusione ni altere otras filas del mismo producto.
 */
const Cart = {
    STORAGE_KEY: 'unisof_cart',

    newLineId() {
        if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
            return crypto.randomUUID();
        }
        return 'ln-' + Date.now() + '-' + Math.random().toString(36).slice(2, 11);
    },

    /**
     * Prefijo del ítem sin la talla (último segmento tras el último guion del id legado).
     * Con `lineId`, `idBase` debe ir siempre guardado en el objeto.
     */
    deriveIdBase(item) {
        if (!item) return '';
        if (item.idBase) return item.idBase;
        if (!item.id) return '';
        return item.id.replace(/-[^-]*$/, '');
    },

    /**
     * Talla efectiva: si el id es el legado idBase-talla, el id manda; si no, la propiedad `size`.
     */
    deriveSize(item) {
        if (!item) return 'M';
        const base = this.deriveIdBase(item);
        const fromId =
            base && item.id && item.id.startsWith(base + '-') ? item.id.slice(base.length + 1) : null;
        const stored =
            item.size != null && String(item.size).trim() !== '' ? String(item.size).trim() : null;
        if (fromId && stored && fromId !== stored) {
            return fromId;
        }
        if (stored) return stored;
        if (fromId) return fromId;
        return 'M';
    },

    normalizeItem(item) {
        if (!item || typeof item !== 'object') return item;
        const hadLineId = !!item.lineId;
        const lineId = item.lineId || this.newLineId();
        const legacyId = !hadLineId && item.id ? String(item.id).trim() : '';

        const idBase =
            item.idBase && String(item.idBase).trim()
                ? item.idBase
                : legacyId && legacyId.includes('-')
                  ? legacyId.replace(/-[^-]*$/, '')
                  : this.deriveIdBase({ ...item, id: legacyId || undefined });

        const size = this.deriveSize({ ...item, idBase, id: legacyId || item.id });

        return { ...item, lineId, idBase, size, id: lineId };
    },

    getItems() {
        try {
            const data = localStorage.getItem(this.STORAGE_KEY);
            const raw = data ? JSON.parse(data) : [];
            if (!Array.isArray(raw)) return [];
            let needSave = false;
            const items = raw.map((orig) => {
                if (!orig.lineId) needSave = true;
                return this.normalizeItem(orig);
            });
            if (needSave) this.saveItems(items);
            return items;
        } catch {
            return [];
        }
    },

    saveItems(items) {
        const normalized = Array.isArray(items) ? items.map((i) => this.normalizeItem(i)) : [];
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(normalized));
        this.onUpdate?.();
    },

    /** Parsea precio "429.000 COP" -> 429000 */
    parsePrice(priceStr) {
        if (typeof priceStr !== 'string') return 0;
        const num = priceStr.replace(/[^\d]/g, '');
        return parseInt(num, 10) || 0;
    },

    addItem(product) {
        const size = product.size || 'M';
        const idBase = product.idBase || product.id || '';
        const items = this.getItems();
        const existing = items.find(
            (i) => (i.idBase || this.deriveIdBase(i)) === idBase && this.deriveSize(i) === size
        );
        if (existing) {
            existing.quantity += 1;
        } else {
            const lineId = this.newLineId();
            items.push({
                ...product,
                idBase,
                lineId,
                id: lineId,
                size,
                quantity: product.quantity != null ? product.quantity : 1
            });
        }
        this.saveItems(items);
    },

    removeItem(id) {
        const items = this.getItems().filter((i) => i.id !== id);
        this.saveItems(items);
    },

    updateQuantity(id, quantity) {
        if (quantity < 1) {
            this.removeItem(id);
            return;
        }
        const items = this.getItems();
        const item = items.find((i) => i.id === id);
        if (item) {
            item.quantity = quantity;
            this.saveItems(items);
        }
    },

    /** Agrega otra línea del mismo producto con una talla distinta (ej: 2M + 1S). */
    addItemVariant(productIdBase) {
        const items = this.getItems();
        const existing = items.find((i) => this.deriveIdBase(i) === productIdBase);
        if (!existing) return;
        const sizes = ['XS', 'S', 'M', 'L', 'XL', '2XL', '3XL'];
        const usedSizes = items
            .filter((i) => this.deriveIdBase(i) === productIdBase)
            .map((i) => this.deriveSize(i));
        const freeSize = sizes.find((s) => !usedSizes.includes(s)) || 'M';
        const lineId = this.newLineId();
        items.push({
            idBase: productIdBase,
            lineId,
            id: lineId,
            name: existing.name,
            unitPrice: existing.unitPrice,
            imageUrl: existing.imageUrl || '',
            size: freeSize,
            quantity: 1,
            genero: existing.genero || ''
        });
        this.saveItems(items);
    },

    /**
     * Cambia la talla solo de la fila indicada (`id` / `lineId`). No fusiona con otras filas.
     */
    updateSize(lineId, newSize) {
        const items = this.getItems();
        const idx = items.findIndex((i) => i.id === lineId || i.lineId === lineId);
        if (idx === -1) return;
        const item = items[idx];
        const idBase = item.idBase || this.deriveIdBase(item);
        if (this.deriveSize(item) === newSize) return;
        const stableId = item.lineId || item.id;
        const next = items.slice();
        next[idx] = {
            ...item,
            idBase,
            size: newSize,
            lineId: stableId,
            id: stableId
        };
        this.saveItems(next);
    },

    getTotal() {
        return this.getItems().reduce((sum, i) => sum + i.unitPrice * i.quantity, 0);
    },

    getCount() {
        return this.getItems().reduce((sum, i) => sum + i.quantity, 0);
    },

    clear() {
        this.saveItems([]);
    },

    onUpdate: null
};
