# DRDO Facial Recognition Attendance System

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)

A modern biometric attendance system utilizing facial detection and recognition to automate attendance management. Built with Kotlin and powered by OpenCV and SQLite, this system offers real-time operation and offline functionality.

## 📜 Features

- **Real-Time Facial Detection and Recognition**
  - Captures and recognizes faces using pre-trained models
  - Processes live camera feeds in real-time
  - Supports multiple face detection simultaneously

- **Automated Attendance Logging**
  - Direct integration with SQLite database
  - Secure timestamp-based attendance records
  - Tamper-proof logging system

- **Offline Functionality**
  - Complete operation without internet connectivity
  - Local database storage
  - Ideal for remote deployments

- **User-Friendly Interface**
  - Intuitive attendance management
  - Real-time visual feedback
  - Easy record viewing and export options

## 🛠️ Technologies Used

- **Kotlin**: Primary development language
- **OpenCV**: Image and video processing
- **SQLite**: Local database management

## 🚀 How It Works

### System Architecture

1. **Face Detection**
   - Processes live camera feeds
   - Identifies facial regions using pre-trained models
   - Real-time detection and tracking

2. **Face Recognition**
   - Converts detected faces to numerical embeddings
   - Matches against database entries
   - Updates attendance records instantly

3. **Database Management**
   - Local SQLite storage
   - User profile management
   - Secure attendance record maintenance

## 📋 Prerequisites

- Android Studio (latest version)
- Android device with functional camera

## 🔧 Installation

1. Clone the repository:
```bash
git clone https://github.com/ishi-ta-lal/Facial-Recognition-System
```

2. Open project in Android Studio:
```bash
cd drdo-final-frs-app
```

3. Build and run:
   - Connect Android device
   - Enable USB debugging
   - Click "Run" in Android Studio

## 📂 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/              # Kotlin source files
│   │   ├── res/               # Resources
│   │   └── AndroidManifest.xml
├── assets/                    # Pre-trained models
└── data/                      # Database schemas
```

## 📊 Performance

### Accuracy Metrics
- Face Detection: >95% accuracy
- Recognition Speed: <500ms per face
- False Positive Rate: <0.1%

## 🔮 Future Scope

- Cloud integration for larger deployments
- Enhanced low-light performance
- Cross-platform support (iOS)
- Advanced analytics and reporting
- Batch processing capabilities
- Multi-language support

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌟 Acknowledgments

- OpenCV community for computer vision tools
- SQLite for database management
- DRDO for project support and guidance

## 🔄 Version History

- 1.0.0 (Current)
  - Initial release
  - Basic facial recognition
  - SQLite integration
  - Real-time attendance logging

---

Made with ❤️ by Ishita Lal & Aniket Chauhan
