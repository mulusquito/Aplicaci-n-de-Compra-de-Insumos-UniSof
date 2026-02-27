/**
 * Carrito de compras - UNISOF
 * Persiste en localStorage. Usado en ventas.html para el flujo de checkout con Mercado Pago.
 */
const Cart = {
    STORAGE_KEY: 'unisof_cart',

    getItems() {
        try {
            const data = localStorage.getItem(this.STORAGE_KEY);
            return data ? JSON.parse(data) : [];
        } catch {
            return [];
        }
    },

    saveItems(items) {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(items));
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
        const id = (product.idBase || product.id || '') + '-' + size;
        const items = this.getItems();
        const existing = items.find(i => i.id === id);
        if (existing) {
            existing.quantity += 1;
        } else {
            items.push({ ...product, id, size, quantity: 1 });
        }
        this.saveItems(items);
    },

    removeItem(id) {
        const items = this.getItems().filter(i => i.id !== id);
        this.saveItems(items);
    },

    updateQuantity(id, quantity) {
        if (quantity < 1) {
            this.removeItem(id);
            return;
        }
        const items = this.getItems();
        const item = items.find(i => i.id === id);
        if (item) {
            item.quantity = quantity;
            this.saveItems(items);
        }
    },

    /** Cambia la talla de un ítem. Si ya existe el mismo producto con la nueva talla, suma cantidades. */
    updateSize(oldId, newSize) {
        const items = this.getItems();
        const item = items.find(i => i.id === oldId);
        if (!item) return;
        const idBase = item.idBase || (item.size != null ? item.id.replace(/-[^-]*$/, '') : item.id);
        const newId = idBase + '-' + newSize;
        if (newId === oldId) return;
        const { quantity, name, unitPrice, imageUrl } = item;
        const rest = items.filter(i => i.id !== oldId);
        const existing = rest.find(i => i.id === newId);
        if (existing) {
            existing.quantity += quantity;
        } else {
            rest.push({ idBase, id: newId, name, unitPrice, imageUrl: imageUrl || '', size: newSize, quantity });
        }
        this.saveItems(rest);
    },

    getTotal() {
        return this.getItems().reduce((sum, i) => sum + (i.unitPrice * i.quantity), 0);
    },

    getCount() {
        return this.getItems().reduce((sum, i) => sum + i.quantity, 0);
    },

    clear() {
        this.saveItems([]);
    },

    onUpdate: null
};
