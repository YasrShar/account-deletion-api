// api/delete-account.js - نسخة مُبَسَّطة للاختبار
export default async function handler(req, res) {
  try {
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

    console.log('🚀 API تم استدعاؤها');
    console.log('📊 Method:', req.method);
    console.log('📋 Body:', req.body);
    console.log('🔐 Headers:', req.headers.authorization ? 'Token موجود' : 'Token مفقود');
    
    // التحقق من متغيرات البيئة
    console.log('🔧 Environment Variables Check:');
    console.log('GOOGLE_CLIENT_ID:', process.env.GOOGLE_CLIENT_ID ? '✅ موجود' : '❌ مفقود');
    console.log('FIREBASE_PROJECT_ID:', process.env.FIREBASE_PROJECT_ID ? '✅ موجود' : '❌ مفقود');
    console.log('FIREBASE_CLIENT_EMAIL:', process.env.FIREBASE_CLIENT_EMAIL ? '✅ موجود' : '❌ مفقود');
    console.log('FIREBASE_PRIVATE_KEY:', process.env.FIREBASE_PRIVATE_KEY ? '✅ موجود' : '❌ مفقود');
    
    const { email, googleId, action } = req.body;
    
    // التحقق من البيانات المطلوبة
    if (!email || !action || !googleId) {
      return res.status(400).json({ 
        success: false,
        error: 'بيانات ناقصة',
        message: 'البريد الإلكتروني والعمل المطلوب ومعرف Google مطلوبان',
        received: { email: !!email, action: !!action, googleId: !!googleId }
      });
    }
    
    // التحقق من التوكن
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ 
        success: false,
        error: 'غير مخول',
        message: 'مطلوب توكن صحيح',
        receivedHeader: authHeader ? 'موجود ولكن غير صحيح' : 'مفقود'
      });
    }
    
    // محاكاة حذف ناجح (بدون Firebase مؤقتاً)
    if (action === 'delete_account') {
      console.log(`✅ محاكاة حذف الحساب لـ: ${email}`);
      
      return res.status(200).json({ 
        success: true, 
        message: 'تم حذف الحساب بنجاح (محاكاة)',
        email: email,
        googleId: googleId,
        environment: {
          hasGoogleClientId: !!process.env.GOOGLE_CLIENT_ID,
          hasFirebaseConfig: !!(process.env.FIREBASE_PROJECT_ID && process.env.FIREBASE_CLIENT_EMAIL),
        },
        timestamp: new Date().toISOString()
      });
    }
    
    return res.status(400).json({ 
      success: false,
      error: 'عمل غير صحيح',
      message: 'فقط عملية delete_account مدعومة',
      receivedAction: action
    });
    
  } catch (error) {
    console.error('❌ خطأ في API:', error);
    console.error('📝 Stack trace:', error.stack);
    
    return res.status(500).json({ 
      success: false,
      error: 'خطأ داخلي في الخادم',
      message: error.message,
      stack: process.env.NODE_ENV === 'development' ? error.stack : undefined,
      timestamp: new Date().toISOString()
    });
  }
}
