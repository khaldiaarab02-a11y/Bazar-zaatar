const CONFIG = window.BAZAR_FIREBASE_CONFIG || {};
const hasFirebase = ['apiKey','authDomain','projectId','appId'].every(k => Boolean(CONFIG[k]));
let db, auth, firebaseUser = null;
let lang = localStorage.getItem('bz-lang') || 'ar';
let activeCategory = 'all';
let cart = JSON.parse(localStorage.getItem('bz-cart') || '[]');
const demoProducts = [
  {id:'demo-1',categoryId:'men',nameAr:'قميص كلاسيكي أنيق',nameFr:'Chemise classique élégante',priceDzd:3900,icon:'👔',featured:true},
  {id:'demo-2',categoryId:'shirts',nameAr:'تيشيرت قطني أساسي',nameFr:'T-shirt essentiel en coton',priceDzd:2200,icon:'👕',newArrival:true},
  {id:'demo-3',categoryId:'men',nameAr:'سترة رجالية عصرية',nameFr:'Veste homme moderne',priceDzd:6500,icon:'🧥',featured:true},
  {id:'demo-4',categoryId:'shoes',nameAr:'حذاء يومي مريح',nameFr:'Chaussures confort',priceDzd:5400,icon:'👟'},
  {id:'demo-5',categoryId:'kids',nameAr:'طقم أطفال أنيق',nameFr:'Ensemble enfant élégant',priceDzd:3200,icon:'🧒',newArrival:true},
  {id:'demo-6',categoryId:'shirts',nameAr:'قميص كاجوال',nameFr:'Chemise casual',priceDzd:3500,icon:'👔'},
  {id:'demo-7',categoryId:'kids',nameAr:'ملابس أطفال يومية',nameFr:'Vêtements enfant',priceDzd:2800,icon:'🧸'},
  {id:'demo-8',categoryId:'shoes',nameAr:'حذاء رياضي',nameFr:'Sneakers sport',priceDzd:5900,icon:'👟'}
];
let products = [...demoProducts];
const words = {
 ar:{delivery:'توصيل داخل تيارت',welcome:'أناقة تليق بك وبعائلتك',home:'الرئيسية',shop:'المتجر',about:'من نحن',contact:'اتصل بنا',cart:'السلة',heroEyebrow:'اختيارات منتقاة بعناية',heroTitle:'أناقة الرجال<br/><em>وأزياء الأطفال</em>',heroText:'اكتشف تشكيلتنا الجديدة من الملابس والإكسسوارات، بجودة وأناقة في قلب تيارت.',discover:'اكتشف المجموعة',findUs:'اكتشف متجرنا ↗',quality:'جودة مختارة',style:'أناقة يومية',local:'من تيارت',benefit1:'تشكيلة متجددة',benefit1s:'قطع مختارة لكل موسم',benefit2:'للرجال والأطفال',benefit2s:'أناقة لكل أفراد العائلة',benefit3:'متواجدون في تيارت',benefit3s:'زورونا في متجرنا',benefit4:'خدمة قريبة منك',benefit4s:'نرافقك من الاختيار إلى الطلب',curated:'تسوق حسب ذوقك',collection:'تشكيلة <em>بازار زعتر</em>',collectionText:'اكتشف القطع التي تناسب أسلوبك.',all:'الكل',men:'ملابس رجالية',shirts:'قمصان وتيشيرتات',shoes:'أحذية',kids:'أزياء الأطفال',ourStory:'حكايتنا',storyTitle:'الأناقة تبدأ من <em>التفاصيل</em>',storyText:'في بازار زعتر، نؤمن أن الأناقة ليست مجرد ملابس، بل أسلوب يعبر عنك. اخترنا لكم تشكيلة للرجال والأطفال تجمع بين الذوق والجودة.',visit:'يسعدنا استقبالكم في متجرنا ↗',comeBy:'نحن قريبون منك',contactTitle:'زورونا في <em>تيارت</em>',map:'افتح الموقع على الخريطة ↗',addressTitle:'عنوان المتجر',contactHint:'للطلب والاستفسار، تواصلوا معنا عبر الهاتف أو صفحاتنا الاجتماعية.',rights:'جميع الحقوق محفوظة.',yourCart:'سلة مشترياتك',total:'المجموع',checkout:'إتمام الطلب',cash:'الدفع عند الاستلام',orderDetails:'معلومات الطلب',fullName:'الاسم الكامل',phone:'رقم الهاتف',deliveryAddress:'عنوان التوصيل',notes:'ملاحظات (اختياري)',confirmOrder:'تأكيد الطلب · الدفع عند الاستلام',added:'تمت إضافة المنتج إلى السلة',empty:'سلتك فارغة حالياً',noProducts:'لا توجد منتجات في هذا التصنيف حالياً',firebaseDemo:'وضع العرض: المنتجات تجريبية. أضف إعدادات Firebase لتفعيل المتجر الحقيقي.',orderOk:'تم تسجيل طلبك بنجاح! سنتواصل معك لتأكيده.',orderDemo:'تم استلام طلبك في وضع العرض التجريبي. اربط Firebase لحفظ الطلبات فعلياً.',remove:'حذف'},
 fr:{delivery:'Livraison à Tiaret',welcome:'Le style pour vous et votre famille',home:'Accueil',shop:'Boutique',about:'À propos',contact:'Contact',cart:'Panier',heroEyebrow:'Une sélection soigneusement choisie',heroTitle:'Mode Homme<br/><em>& Enfants</em>',heroText:'Découvrez notre nouvelle collection de vêtements et accessoires, au cœur de Tiaret.',discover:'Découvrir la collection',findUs:'Découvrir la boutique ↗',quality:'Qualité choisie',style:'Style quotidien',local:'De Tiaret',benefit1:'Collection renouvelée',benefit1s:'Des pièces pour chaque saison',benefit2:'Homme & Enfants',benefit2s:'Du style pour toute la famille',benefit3:'À Tiaret',benefit3s:'Rendez-nous visite',benefit4:'À votre écoute',benefit4s:'Du choix à la commande',curated:'Achetez selon votre style',collection:'La collection <em>Bazar Zaatar</em>',collectionText:'Trouvez les pièces qui vous ressemblent.',all:'Tout',men:'Homme',shirts:'Chemises & T-shirts',shoes:'Chaussures',kids:'Enfants',ourStory:'Notre histoire',storyTitle:'Le style est dans <em>les détails</em>',storyText:'Chez Bazar Zaatar, le style est plus qu’un vêtement : c’est une expression de soi. Découvrez notre sélection Homme & Enfants, entre goût et qualité.',visit:'Nous serons ravis de vous accueillir ↗',comeBy:'Nous sommes près de vous',contactTitle:'Rendez-nous visite à <em>Tiaret</em>',map:'Voir sur la carte ↗',addressTitle:'Adresse du magasin',contactHint:'Pour toute commande ou question, contactez-nous par téléphone ou sur nos réseaux.',rights:'Tous droits réservés.',yourCart:'Votre panier',total:'Total',checkout:'Commander',cash:'Paiement à la livraison',orderDetails:'Informations de commande',fullName:'Nom complet',phone:'Téléphone',deliveryAddress:'Adresse de livraison',notes:'Notes (facultatif)',confirmOrder:'Confirmer · Paiement à la livraison',added:'Produit ajouté au panier',empty:'Votre panier est vide',noProducts:'Aucun produit dans cette catégorie',firebaseDemo:'Mode aperçu : produits de démonstration. Configurez Firebase pour activer la boutique.',orderOk:'Commande enregistrée ! Nous vous contacterons pour la confirmer.',orderDemo:'Commande reçue en mode aperçu. Configurez Firebase pour enregistrer les commandes.',remove:'Supprimer'}
};
const t = key => words[lang][key] || key;
const money = n => `${Number(n||0).toLocaleString(lang==='ar'?'ar-DZ':'fr-FR')} دج`;
function setLang(next){lang=next;localStorage.setItem('bz-lang',lang);document.documentElement.lang=lang;document.documentElement.dir=lang==='ar'?'rtl':'ltr';document.querySelectorAll('[data-i]').forEach(el=>{const v=t(el.dataset.i); if(el.matches('label')){const control=el.querySelector('input,textarea,select');if(control){const firstText=[...el.childNodes].find(n=>n.nodeType===Node.TEXT_NODE);if(firstText)firstText.textContent=v;else el.insertBefore(document.createTextNode(v),control);}else el.textContent=v;}else if(v.includes('<'))el.innerHTML=v;else el.textContent=v});document.getElementById('langBtn').textContent=lang==='ar'?'FR':'عربي';document.getElementById('dataNote').textContent=t('firebaseDemo');renderProducts();renderCart()}
function productName(p){return lang==='ar'?(p.nameAr||p.nameFr):(p.nameFr||p.nameAr)}
function renderProducts(){const list=products.filter(p=>p.active!==false&&(activeCategory==='all'||(activeCategory==='men'?['men','mens_clothing','mens_trousers'].includes(p.categoryId):activeCategory==='shirts'?['shirts','mens_tshirts','mens_shirts'].includes(p.categoryId):activeCategory==='shoes'?['shoes','mens_shoes'].includes(p.categoryId):['kids','kids_clothing','boys_clothing','kids_new_arrivals'].includes(p.categoryId))));document.getElementById('products').innerHTML=list.length?list.map(p=>`<article class="product"><div class="product-photo">${p.imageUrl?`<img src="${escapeHtml(p.imageUrl)}" alt="" style="width:100%;height:100%;object-fit:cover">`:(p.icon||'✦')} ${p.specialOffer?`<span class="tag">${lang==='ar'?'عرض خاص':'Offre'}</span>`:''}</div><div class="product-info"><small>${p.newArrival?(lang==='ar'?'وصل حديثاً':'NOUVEAUTÉ'):(p.featured?(lang==='ar'?'اختيارنا':'SÉLECTION'):(lang==='ar'?'بازار زعتر':'BAZAR ZAATAR'))}</small><h3>${escapeHtml(productName(p))}</h3><div class="product-bottom"><span class="price">${money(p.discountPriceDzd||p.priceDzd)}</span><button class="add" data-add="${p.id}" aria-label="Add">+</button></div></div></article>`).join(''):`<div class="empty">${t('noProducts')}</div>`;document.querySelectorAll('[data-add]').forEach(b=>b.addEventListener('click',()=>addToCart(b.dataset.add)))}
function escapeHtml(s=''){return String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]))}
function addToCart(id){const p=products.find(x=>x.id===id);if(!p)return;const item=cart.find(x=>x.id===id);if(item){if(item.qty>=99)return;item.qty++}else cart.push({id,qty:1});saveCart();renderCart()}
function saveCart(){localStorage.setItem('bz-cart',JSON.stringify(cart));document.getElementById('cartCount').textContent=cart.reduce((s,x)=>s+x.qty,0)}
function renderCart(){const target=document.getElementById('cartItems');target.innerHTML=cart.length?cart.map(row=>{const p=products.find(x=>x.id===row.id)||demoProducts.find(x=>x.id===row.id);if(!p)return '';return `<div class="cart-row"><div class="cart-emoji">${p.icon||'✦'}</div><div><b>${escapeHtml(productName(p))}</b><small>${money(p.discountPriceDzd||p.priceDzd)}</small></div><div class="qty"><button data-dec="${p.id}">−</button><b>${row.qty}</b><button data-inc="${p.id}">+</button></div></div>`}).join(''):`<div class="empty">${t('empty')}</div>`;const total=cart.reduce((s,row)=>{const p=products.find(x=>x.id===row.id)||demoProducts.find(x=>x.id===row.id);return s+(p?(p.discountPriceDzd||p.priceDzd)*row.qty:0)},0);document.getElementById('cartTotal').textContent=money(total);document.getElementById('checkoutBtn').disabled=(!cart.length || !hasFirebase || !db || !firebaseUser);document.querySelectorAll('[data-dec]').forEach(b=>b.onclick=()=>changeQty(b.dataset.dec,-1));document.querySelectorAll('[data-inc]').forEach(b=>b.onclick=()=>changeQty(b.dataset.inc,1));saveCart()}
function changeQty(id,n){const row=cart.find(x=>x.id===id);if(row){row.qty=Math.min(99,row.qty+n);if(row.qty<=0)cart=cart.filter(x=>x.id!==id);saveCart();renderCart()}}
function openCart(){document.getElementById('cartPanel').classList.add('open');document.getElementById('overlay').classList.add('show')}function closeCart(){document.getElementById('cartPanel').classList.remove('open');document.getElementById('overlay').classList.remove('show')}
async function setupFirebase(){if(!hasFirebase)return;try{const [{initializeApp},{getAuth,signInAnonymously},{getFirestore,collection,getDocs,query,where}]=await Promise.all([import('https://www.gstatic.com/firebasejs/10.12.5/firebase-app.js'),import('https://www.gstatic.com/firebasejs/10.12.5/firebase-auth.js'),import('https://www.gstatic.com/firebasejs/10.12.5/firebase-firestore.js')]);const app=initializeApp(CONFIG);auth=getAuth(app);db=getFirestore(app, CONFIG.databaseId || '(default)');const cred=await signInAnonymously(auth);firebaseUser=cred.user;const q=query(collection(db,'products'),where('active','==',true));const snap=await getDocs(q);products=snap.docs.map(d=>({id:d.id,...d.data()}));document.getElementById('dataNote').hidden=true;renderProducts();window.__bzFirebase={db,auth,app};renderCart();}catch(e){console.warn('Firebase unavailable; using preview data.',e);document.getElementById('dataNote').textContent=lang==='ar'?'تعذر الاتصال بـ Firebase. الطلبات متوقفة حتى استعادة الاتصال.':'Firebase indisponible. Les commandes sont suspendues jusqu’au rétablissement de la connexion.';document.getElementById('dataNote').hidden=false;renderCart();}}
async function submitOrder(form){
 const status=document.getElementById('formStatus');
 const data=Object.fromEntries(new FormData(form));
 try{
  if(!cart.length) throw new Error('Cart is empty');
  if(hasFirebase&&(!db||!firebaseUser)) throw new Error('Firebase is configured but the customer session is not ready.');
  if(cart.some(row=>!products.some(p=>p.id===row.id))) throw new Error('The cart contains unavailable products. Refresh the catalogue.');
  if(!hasFirebase || !db || !firebaseUser) throw new Error('Live Firebase session is required to submit an order.');
  if(cart.length > 10) throw new Error('A maximum of 10 cart lines can be placed in one order.');
  const [{collection,addDoc,serverTimestamp}]=await Promise.all([
   import('https://www.gstatic.com/firebasejs/10.12.5/firebase-firestore.js')
  ]);
  const orderItems=cart.map(row=>{
   const product=products.find(p=>p.id===row.id);
   if(!product || product.active!==true || product.inStock!==true) throw new Error('A product is unavailable. Refresh the catalogue.');
   const stock=Number(product.stockQuantity);
   if(!Number.isInteger(stock) || stock < row.qty) throw new Error('Requested quantity exceeds the available stock.');
   const basePrice=Number(product.priceDzd);
   const discountPrice=Number(product.discountPriceDzd);
   const unitPriceDzd=Number.isFinite(discountPrice)&&discountPrice>0&&discountPrice<basePrice?discountPrice:basePrice;
   if(!Number.isFinite(unitPriceDzd)||unitPriceDzd<=0) throw new Error('Invalid product price.');
   return {productId:product.id,nameAr:String(product.nameAr||product.nameFr||''),nameFr:String(product.nameFr||product.nameAr||''),size:'',color:'',quantity:row.qty,unitPriceDzd};
  });
  const subtotalDzd=orderItems.reduce((sum,item)=>sum+item.quantity*item.unitPriceDzd,0);
  await addDoc(collection(db,'orders'),{
   userId:firebaseUser.uid,
   customerName:String(data.customerName||'').trim(),
   phoneNumber:String(data.phoneNumber||'').trim(),
   deliveryAddress:String(data.deliveryAddress||'').trim(),
   notes:String(data.notes||'').trim(),
   items:orderItems,
   subtotalDzd,
   totalDzd:subtotalDzd,
   status:'pending',
   createdAt:serverTimestamp(),
   updatedAt:serverTimestamp()
  });
  status.textContent=t('orderOk');
  cart=[];saveCart();renderCart();form.reset();
  setTimeout(()=>{document.getElementById('checkoutDialog').close();closeCart();status.textContent=''},3500);
 }catch(e){
  console.error('Order submission failed:',e);
  status.textContent=lang==='ar'?'لم نتمكن من حفظ الطلب. تحقق من الاتصال وقواعد Firestore ثم حدّث الصفحة.':'Impossible d’enregistrer la commande. Vérifiez la connexion et les règles Firestore, puis actualisez la page.';
 }
}
document.getElementById('langBtn').onclick=()=>setLang(lang==='ar'?'fr':'ar');document.getElementById('cartBtn').onclick=openCart;document.getElementById('closeCart').onclick=closeCart;document.getElementById('overlay').onclick=closeCart;document.getElementById('checkoutBtn').onclick=()=>{if(cart.length)document.getElementById('checkoutDialog').showModal()};document.getElementById('closeDialog').onclick=()=>document.getElementById('checkoutDialog').close();document.getElementById('checkoutForm').onsubmit=e=>{e.preventDefault();submitOrder(e.currentTarget)};document.getElementById('menuBtn').onclick=()=>document.getElementById('nav').classList.toggle('open');document.querySelectorAll('#nav a').forEach(a=>a.onclick=()=>document.getElementById('nav').classList.remove('open'));document.querySelectorAll('.filter').forEach(b=>b.onclick=()=>{document.querySelectorAll('.filter').forEach(x=>x.classList.remove('active'));b.classList.add('active');activeCategory=b.dataset.cat;renderProducts()});document.getElementById('year').textContent=new Date().getFullYear();setLang(lang);renderProducts();renderCart();setupFirebase();
if('serviceWorker'in navigator)window.addEventListener('load',()=>navigator.serviceWorker.register('/sw.js').catch(()=>{}));
