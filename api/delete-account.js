// api/delete-account.js
import { OAuth2Client } from 'google-auth-library';
import admin from 'firebase-admin';

// إعداد Firebase Admin SDK
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey: process.env.FIREBASE_PRIVATE_KEY?.replace(/\\n/g, '\n'),
    }),
  });
}

// إعداد Google OAuth Client
const client = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

export default async function handler(req, res) {
  // إعداد CORS
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader('Access-Control-Allow-Headers', 'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Authorization');
  
  // معالجة طلب OPTIONS
  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }
  
  // قبول POST requests فقط
  if (req.method !== 'POST') {
    return res.status(405).json({ 
      error: 'Method not allowed',
      message: 'Only POST requests are accepted' 
    });
  }

  try {
    console.log('تم استقبال طلب:', req.body);
    
    const { email, googleId, action } = req.body;
    
    // التحقق من البيانات المطلوبة
    if (!email || !action || !googleId) {
      return res.status(400).json({ 
        error: 'بيانات ناقصة',
        message: 'البريد الإلكتروني والعمل المطلوب ومعرف Google مطلوبان' 
      });
    }

    // التحقق من التوكن
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ 
        error: 'غير مخول',
        message: 'مطلوب توكن صحيح' 
      });
    }

    const idToken = authHeader.split(' ')[1];
    
    // التحقق من صحة التوكن مع Google
    let payload;
    try {
      const ticket = await client.verifyIdToken({
        idToken: idToken,
        audience: process.env.GOOGLE_CLIENT_ID,
      });
      payload = ticket.getPayload();
      
      // التأكد أن التوكن يخص نفس المستخدم
      if (payload.email !== email || payload.sub !== googleId) {
        return res.status(403).json({
          error: 'التوكن لا يطابق بيانات المستخدم',
          message: 'التوكن المُرسل لا يخص هذا المستخدم'
        });
      }
    } catch (error) {
      console.error('خطأ في التحقق من التوكن:', error);
      return res.status(401).json({
        error: 'توكن غير صحيح',
        message: 'فشل التحقق من التوكن'
      });
    }

    // معالجة طلب حذف الحساب
    if (action === 'delete_account') {
      console.log(`معالجة حذف الحساب لـ: ${email}`);
      
      let firebaseUid = null;
      
      try {
        // 1. البحث عن المستخدم في Firebase بالإيميل
        const userRecord = await admin.auth().getUserByEmail(email);
        firebaseUid = userRecord.uid;
        console.log(`تم العثور على المستخدم في Firebase: ${firebaseUid}`);
        
        // 2. حذف البيانات من قاعدة البيانات أولاً
        await deleteAccountFromDatabase(email, googleId, firebaseUid);
        
        // 3. حذف المستخدم من Firebase Authentication
        await admin.auth().deleteUser(firebaseUid);
        console.log(`تم حذف المستخدم من Firebase Authentication: ${firebaseUid}`);
        
      } catch (firebaseError) {
        console.error('خطأ Firebase:', firebaseError);
        
        // إذا لم يتم العثور على المستخدم في Firebase، احذف من قاعدة البيانات فقط
        if (firebaseError.code === 'auth/user-not-found') {
          console.log('المستخدم غير موجود في Firebase، حذف من قاعدة البيانات فقط');
          await deleteAccountFromDatabase(email, googleId, null);
        } else {
          throw firebaseError;
        }
      }
      
      // إرسال رد بنجح العملية
      return res.status(200).json({ 
        success: true, 
        message: 'تم حذف الحساب بنجاح من التطبيق و Firebase',
        email: email,
        firebaseUid: firebaseUid,
        timestamp: new Date().toISOString()
      });
    }
    
    return res.status(400).json({ 
      error: 'عمل غير صحيح',
      message: 'فقط عملية delete_account مدعومة' 
    });
    
  } catch (error) {
    console.error('خطأ في API:', error);
    return res.status(500).json({ 
      error: 'خطأ داخلي في الخادم',
      message: error.message,
      timestamp: new Date().toISOString()
    });
  }
}

// دالة حذف الحساب من قاعدة البيانات
async function deleteAccountFromDatabase(email, googleId, firebaseUid) {
  console.log(`حذف البيانات: ${email} (Google: ${googleId}, Firebase: ${firebaseUid})`);
  
  try {
    // هنا ضع كود حذف البيانات من قاعدة البيانات الخاصة بك
    // مثال مع Firestore:
    /*
    const db = admin.firestore();
    
    // حذف مستندات المستخدم
    await db.collection('users').doc(firebaseUid || googleId).delete();
    
    // حذف بيانات المستخدم الأخرى
    const userDataQuery = db.collection('user_data').where('userId', '==', firebaseUid || googleId);
    const userDataSnapshot = await userDataQuery.get();
    
    const batch = db.batch();
    userDataSnapshot.docs.forEach((doc) => {
      batch.delete(doc.ref);
    });
    
    await batch.commit();
    */
    
    // محاكاة عملية حذف قاعدة البيانات
    return new Promise((resolve) => {
      setTimeout(() => {
        console.log('تم حذف البيانات من قاعدة البيانات');
        resolve();
      }, 1000);
    });
    
  } catch (error) {
    console.error('خطأ في حذف البيانات:', error);
    throw error;
  }
}
