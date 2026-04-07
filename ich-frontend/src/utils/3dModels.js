import * as THREE from 'three'

// 生成剪纸模型（扁平化、镂空效果）
export function createPapercutModel() {
  const group = new THREE.Group()
  
  // 主轮廓 - 圆形剪纸
  const geometry = new THREE.CylinderGeometry(1.2, 1.2, 0.05, 32)
  const material = new THREE.MeshStandardMaterial({
    color: 0xd43f1d,
    roughness: 0.3,
    metalness: 0.1,
    emissive: 0x441100,
    emissiveIntensity: 0.2
  })
  const base = new THREE.Mesh(geometry, material)
  base.castShadow = true
  base.receiveShadow = true
  group.add(base)
  
  // 剪纸镂空图案 - 中心花朵形状
  const holePositions = [
    { angle: 0, radius: 0.6, size: 0.25 },
    { angle: Math.PI / 3, radius: 0.7, size: 0.2 },
    { angle: Math.PI * 2 / 3, radius: 0.7, size: 0.2 },
    { angle: Math.PI, radius: 0.6, size: 0.25 },
    { angle: Math.PI * 4 / 3, radius: 0.7, size: 0.2 },
    { angle: Math.PI * 5 / 3, radius: 0.7, size: 0.2 }
  ]
  
  holePositions.forEach(pos => {
    const holeGeo = new THREE.SphereGeometry(pos.size, 24, 24)
    const holeMat = new THREE.MeshStandardMaterial({ color: 0x331100, emissive: 0x221100 })
    const hole = new THREE.Mesh(holeGeo, holeMat)
    hole.position.x = Math.cos(pos.angle) * pos.radius
    hole.position.z = Math.sin(pos.angle) * pos.radius
    hole.position.y = 0.03
    group.add(hole)
  })
  
  // 添加金色边框
  const borderGeo = new THREE.TorusGeometry(1.25, 0.05, 32, 100)
  const borderMat = new THREE.MeshStandardMaterial({ color: 0xd4af37, metalness: 0.8, roughness: 0.2 })
  const border = new THREE.Mesh(borderGeo, borderMat)
  border.rotation.x = Math.PI / 2
  border.position.y = 0.04
  group.add(border)
  
  return group
}

// 生成青花瓷模型
export function createPorcelainModel() {
  const group = new THREE.Group()
  
  // 瓶身曲线点
  const points = [
    [0, -1.2, 0], [0.3, -1, 0], [0.5, -0.5, 0], [0.55, 0, 0],
    [0.5, 0.5, 0], [0.4, 0.8, 0], [0.3, 1, 0], [0.15, 1.1, 0], [0, 1.15, 0]
  ]
  
  const lathePoints = points.map(p => new THREE.Vector2(p[0], p[1]))
  const geometry = new THREE.LatheGeometry(lathePoints, 48)
  const material = new THREE.MeshStandardMaterial({
    color: 0x88aacc,
    roughness: 0.2,
    metalness: 0.7,
    emissive: 0x224466,
    emissiveIntensity: 0.1
  })
  const body = new THREE.Mesh(geometry, material)
  body.castShadow = true
  body.receiveShadow = true
  group.add(body)
  
  // 青花瓷纹饰 - 蓝色环带
  const ringPositions = [-0.6, -0.2, 0.2, 0.6]
  ringPositions.forEach(yPos => {
    const ringGeo = new THREE.TorusGeometry(0.48, 0.03, 32, 64)
    const ringMat = new THREE.MeshStandardMaterial({ color: 0x2266aa, metalness: 0.4 })
    const ring = new THREE.Mesh(ringGeo, ringMat)
    ring.rotation.x = Math.PI / 2
    ring.position.y = yPos
    group.add(ring)
  })
  
  // 颈部纹饰
  const neckGeo = new THREE.TorusGeometry(0.22, 0.02, 32, 48)
  const neckMat = new THREE.MeshStandardMaterial({ color: 0x3366cc })
  const neckRing = new THREE.Mesh(neckGeo, neckMat)
  neckRing.rotation.x = Math.PI / 2
  neckRing.position.y = 1.0
  group.add(neckRing)
  
  return group
}

// 生成皮影模型（带关节可动）
export function createShadowPuppetModel(type = 'male') {
  const group = new THREE.Group()
  
  // 颜色配置
  const colors = {
    male: { body: 0xcc8844, detail: 0xaa6644 },
    female: { body: 0xdd9966, detail: 0xcc8855 },
    warrior: { body: 0xaa5533, detail: 0x884422 }
  }
  const colorSet = colors[type] || colors.male
  
  // 头部
  const headGeo = new THREE.SphereGeometry(0.25, 32, 32)
  const headMat = new THREE.MeshStandardMaterial({ color: colorSet.body, roughness: 0.3 })
  const head = new THREE.Mesh(headGeo, headMat)
  head.position.y = 0.85
  group.add(head)
  
  // 头饰
  const hatGeo = new THREE.CylinderGeometry(0.28, 0.32, 0.15, 8)
  const hatMat = new THREE.MeshStandardMaterial({ color: 0xd4af37, metalness: 0.6 })
  const hat = new THREE.Mesh(hatGeo, hatMat)
  hat.position.y = 1.02
  group.add(hat)
  
  // 身体
  const bodyGeo = new THREE.CylinderGeometry(0.28, 0.32, 0.7, 8)
  const bodyMat = new THREE.MeshStandardMaterial({ color: colorSet.body })
  const bodyMesh = new THREE.Mesh(bodyGeo, bodyMat)
  bodyMesh.position.y = 0.4
  group.add(bodyMesh)
  
  // 左臂（独立物体，可动）
  const leftArmGeo = new THREE.BoxGeometry(0.18, 0.5, 0.12)
  const armMat = new THREE.MeshStandardMaterial({ color: colorSet.body })
  const leftArm = new THREE.Mesh(leftArmGeo, armMat)
  leftArm.position.set(-0.45, 0.65, 0)
  leftArm.userData = { isJoint: true, name: 'leftArm' }
  group.add(leftArm)
  
  // 右臂
  const rightArm = new THREE.Mesh(leftArmGeo.clone(), armMat)
  rightArm.position.set(0.45, 0.65, 0)
  rightArm.userData = { isJoint: true, name: 'rightArm' }
  group.add(rightArm)
  
  // 服饰装饰
  const decorGeo = new THREE.SphereGeometry(0.08, 8, 8)
  const decorMat = new THREE.MeshStandardMaterial({ color: 0xd4af37 })
  const decor1 = new THREE.Mesh(decorGeo, decorMat)
  decor1.position.set(-0.2, 0.45, 0.15)
  group.add(decor1)
  
  const decor2 = new THREE.Mesh(decorGeo, decorMat)
  decor2.position.set(0.2, 0.45, 0.15)
  group.add(decor2)
  
  // 存储关节引用
  group.userData = { joints: { leftArm, rightArm } }
  
  return group
}

// 动画函数：摆动皮影手臂
export function animateShadowPuppet(model, time) {
  if (!model || !model.userData?.joints) return
  
  const angle = Math.sin(time * 3) * 0.5
  const leftArm = model.userData.joints.leftArm
  const rightArm = model.userData.joints.rightArm
  
  if (leftArm) {
    leftArm.rotation.z = angle * 0.8
  }
  if (rightArm) {
    rightArm.rotation.z = -angle * 0.6
  }
}

// 获取模型列表
export const modelLibrary = {
  papercut: { name: '剪纸·福字纹样', creator: createPapercutModel, type: '剪纸' },
  porcelain: { name: '青花瓷·缠枝莲纹', creator: createPorcelainModel, type: '瓷器' },
  shadowMale: { name: '皮影·生角', creator: () => createShadowPuppetModel('male'), type: '皮影' },
  shadowFemale: { name: '皮影·旦角', creator: () => createShadowPuppetModel('female'), type: '皮影' },
  shadowWarrior: { name: '皮影·武将', creator: () => createShadowPuppetModel('warrior'), type: '皮影' }
}