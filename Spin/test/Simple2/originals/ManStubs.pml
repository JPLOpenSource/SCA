// ------------------------------------------------------
// Manually coded Promela functions
// ------------------------------------------------------


inline Guard_myGuard(retVal)
{
  if 
  :: retVal = true
  :: retVal = false
  fi
}

inline Guard_s1Enter()
{
  printf("Guard_s1Enter()\n");
}

inline Guard_s1Exit()
{
  printf("Guard_s1Exit()\n");
}

inline Guard_s2Enter()
{
  printf("Guard_s2Enter()\n");
}

inline Guard_s2Exit()
{
  printf("Guard_s2Exit()\n");
}

inline Guard_s2Process()
{
  printf("Guard_s2Process()\n");
}
