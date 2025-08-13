// api/delete-account.js
import { OAuth2Client } from 'google-auth-library';

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
      
      // حذف الحساب من قاعدة البيانات
      await deleteAccountFromDatabase(email, googleId);
      
      // إرسال رد بنجح العملية
      return res.status(200).json({ 
        success: true, 
        message: 'تم حذف الحساب بنجاح',
        email: email,
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
async function deleteAccountFromDatabase(email, googleId) {
  // هنا يجب أن تضع كود حذف البيانات من قاعدة البيانات الخاصة بك
  // مثال:
  // await db.users.deleteOne({ email: email, googleId: googleId });
  // await db.user_data.deleteMany({ userId: googleId });
  
  console.log(`تم حذف الحساب: ${email} (${googleId})`);
  
  // محاكاة عملية حذف قاعدة البيانات
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve();
    }, 1000);
  });
}
