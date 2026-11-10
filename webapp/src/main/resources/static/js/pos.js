/* POS page: menu, cart, checkout. Element ids are kept stable and descriptive
   for Selenium / WebdriverIO / Playwright locators. */

var cart = {}; // itemId -> { itemId, itemName, price, quantity }
var menuById = {};

document.getElementById('logout-btn').addEventListener('click', logout);

requireLogin().then(function (user) {
    document.getElementById('welcome-user').textContent = 'Hi, ' + user.fullName + ' (' + user.role + ')';
    loadMenu();
});

function loadMenu() {
    apiGet('/api/menu').then(function (items) {
        var container = document.getElementById('menu-list');
        container.innerHTML = '';
        items.forEach(function (item) {
            menuById[item.itemId] = item;

            var card = document.createElement('div');
            card.className = 'menu-item';
            card.id = 'menu-item-' + item.itemId;

            var title = document.createElement('h3');
            title.textContent = item.itemName;

            var price = document.createElement('div');
            price.className = 'price';
            price.textContent = '$' + Number(item.price).toFixed(2);

            var stock = document.createElement('div');
            stock.style.fontSize = '12px';
            stock.style.color = '#777';
            stock.textContent = 'Stock: ' + item.stockQty;

            var addBtn = document.createElement('button');
            addBtn.id = 'add-item-' + item.itemId;
            addBtn.textContent = 'Add to Cart';
            addBtn.addEventListener('click', function () { addToCart(item); });

            card.appendChild(title);
            card.appendChild(price);
            card.appendChild(stock);
            card.appendChild(addBtn);
            container.appendChild(card);
        });
    });
}

function addToCart(item) {
    if (!cart[item.itemId]) {
        cart[item.itemId] = { itemId: item.itemId, itemName: item.itemName, price: item.price, quantity: 0 };
    }
    cart[item.itemId].quantity += 1;
    renderCart();
}

function changeQty(itemId, delta) {
    var line = cart[itemId];
    if (!line) return;
    line.quantity += delta;
    if (line.quantity <= 0) delete cart[itemId];
    renderCart();
}

function removeFromCart(itemId) {
    delete cart[itemId];
    renderCart();
}

function renderCart() {
    var body = document.getElementById('cart-body');
    body.innerHTML = '';
    var total = 0;

    Object.keys(cart).forEach(function (itemId) {
        var line = cart[itemId];
        var lineTotal = line.price * line.quantity;
        total += lineTotal;

        var row = document.createElement('tr');
        row.id = 'cart-row-' + itemId;

        row.innerHTML =
            '<td>' + line.itemName + '</td>' +
            '<td class="qty-controls">' +
                '<button id="qty-decrease-' + itemId + '">-</button> ' +
                '<span id="qty-value-' + itemId + '">' + line.quantity + '</span> ' +
                '<button id="qty-increase-' + itemId + '">+</button>' +
            '</td>' +
            '<td>$' + Number(line.price).toFixed(2) + '</td>' +
            '<td id="line-total-' + itemId + '">$' + lineTotal.toFixed(2) + '</td>' +
            '<td><button id="remove-' + itemId + '">Remove</button></td>';

        body.appendChild(row);

        row.querySelector('#qty-decrease-' + itemId).addEventListener('click', function () { changeQty(itemId, -1); });
        row.querySelector('#qty-increase-' + itemId).addEventListener('click', function () { changeQty(itemId, 1); });
        row.querySelector('#remove-' + itemId).addEventListener('click', function () { removeFromCart(itemId); });
    });

    document.getElementById('cart-total').textContent = '$' + total.toFixed(2);
    document.getElementById('checkout-btn').disabled = Object.keys(cart).length === 0;
}

document.getElementById('checkout-btn').addEventListener('click', function () {
    var errorBox = document.getElementById('checkout-error');
    var successBox = document.getElementById('checkout-success');
    errorBox.textContent = '';
    successBox.textContent = '';

    var items = Object.keys(cart).map(function (itemId) {
        return { itemId: Number(itemId), quantity: cart[itemId].quantity };
    });

    var payload = {
        items: items,
        paymentMethod: document.getElementById('payment-method').value,
        orderType: 'DINE_IN'
    };

    apiPost('/api/orders/checkout', payload).then(function (order) {
        successBox.id = 'checkout-success';
        successBox.textContent = 'Order ' + order.orderNumber + ' placed successfully. Total: $' + Number(order.totalAmount).toFixed(2);
        successBox.setAttribute('data-order-id', order.orderId);
        cart = {};
        renderCart();
        loadMenu(); // refresh stock counts
    }).catch(function (err) {
        errorBox.textContent = err.message || 'Checkout failed';
    });
});
